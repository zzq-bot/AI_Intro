package controllers.Astar;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractPlayer{
	public ArrayList<ScoreAndState> closeList=new ArrayList<ScoreAndState>();//走过的路径
	public ArrayList<ScoreAndState> openList = new ArrayList<ScoreAndState>();//已探索未走过的路径
	//public ArrayList<ScoreAndState> thisrealList = new ArrayList<ScoreAndState>();//这次寻路采取的路径
	public ArrayList<StateObservation> realList=new ArrayList<StateObservation>();//真实的路径
	public ScoreAndState nowState;//每一次执行Astar对应state
	Vector2d orikeypos; //钥匙的坐标
	public boolean getkey;//是否已经取到钥匙
	//public boolean thistime_getkey;
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer){
		ArrayList<Observation>[] movingPositions = so.getMovablePositions();
		orikeypos = movingPositions[0].get(0).position;
		getkey = false;
	}
	
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		Init();
		if(getKey(stateObs)) {
			getkey = true;
			//Init();
		}
		StateObservation stCopy = stateObs.copy();
		//ScoreAndState fatherState = new ScoreAndState(stCopy,0,10000,null);
		/*fatherState.stateObs = stCopy;
		fatherState.gScore = 0;
		fatherState.score = 0;
		fatherState.father2thisAct = null;*/
		nowState = new ScoreAndState(stCopy,0,10000,null);
		nowState.father = null;
		openList.add(nowState);
		realList.add(stCopy);
		Types.ACTIONS action = null;
		
		double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;
        int remainingLimit = 7;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
        //while(true) {
        	if(nowState.stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS)
        		break;
        	if(getKey(nowState.stateObs)&!getkey)
        		break;
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            //跑到没时间为止
            Astar(nowState);
            /*if(thisrealList.size()>1)
            	action = thisrealList.get(1).father2thisAct;
            */
            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;

            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        	}
        action = FindAct();
        return action;
	}
	
	public void Init() {
		closeList.clear();
		openList.clear();
		//thisrealList.clear();
	}
	
	public void Astar(ScoreAndState fatherState) {//每次Astar在openList找到score最好的状态，然后拓展，更新
		//找到score最好的state作为当前state
		double bestScore = 100000;
		ScoreAndState thisState = null;
		int thisState_id=-1;
		for(int i=0;i<openList.size();i++) {
			if(openList.get(i).score<bestScore) {
				bestScore = openList.get(i).score;
				thisState = openList.get(i);
				thisState_id = i; 
			}
		}
		//更新此时的新路径并将此state加入closeList
		nowState = thisState;
		if(thisState.stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS)
			return;
		if(getKey(nowState.stateObs)&!getkey)
			return;
		openList.remove(thisState_id);
		closeList.add(thisState);
		//探寻新路径更新并加入openList
		StateObservation stCopy = thisState.stateObs.copy();
		ArrayList<Types.ACTIONS> availableActions = stCopy.getAvailableActions();
		for(int i=0;i<availableActions.size();i++) {
			stCopy = thisState.stateObs.copy();
			Types.ACTIONS thisact = availableActions.get(i);
			stCopy.advance(thisact);
			if(stCopy.getGameWinner()==Types.WINNER.PLAYER_LOSES||isVisited(stCopy)) {//如果输了或者已经访问过
				//stCopy = thisState.stateObs.copy();
				continue;
			}
			if(stCopy.getGameWinner()==Types.WINNER.PLAYER_WINS) {
				ScoreAndState newState = new ScoreAndState(stCopy.copy(),thisState.gScore+50,0,thisState);
				newState.father2thisAct = thisact;
				openList.add(newState);
			}
			else if(inOpenList(stCopy)!=-1) {
				//如果此时的stateObservation在openList出现过，就要判断是否更新
				double thisG = thisState.gScore+50;
				int id = inOpenList(stCopy);
				if(thisG<openList.get(id).gScore) {
					//更新
					openList.get(id).father = thisState;
					openList.get(id).father2thisAct = thisact;
					openList.get(id).gScore = thisG;
					openList.get(id).score = openList.get(id).hScore+thisG;
				}
			}
			else {
				//从未探索过的点
				ScoreAndState newState = new ScoreAndState(stCopy.copy(),thisState.gScore+50,heuristic(stCopy),thisState);
				newState.father2thisAct = thisact;
				openList.add(newState);
			}
		}
		
	}

	
	public boolean isVisited(StateObservation stateObs) {
		for(int i=0;i<closeList.size();i++) {
			//System.out.println(closeList.get(i).stateObs.getAvatarPosition());
			//System.out.println(stateObs.getAvatarPosition());
			if(stateObs.equalPosition(closeList.get(i).stateObs)) {
				return true;
			}
		}
		for(int i=0;i<realList.size();i++) {
			if(stateObs.equalPosition(realList.get(i)))
				return true;
		}
		return false;
	}
	
	public int inOpenList(StateObservation stateObs) {
		for(int i=0;i<openList.size();i++) {
			if(stateObs.equalPosition(openList.get(i).stateObs)) {
				return i;
			}
		}
		return -1;
		
	}
	public Types.ACTIONS FindAct(){
		ScoreAndState temp = nowState;
		while(temp.father.father!=null) {
			temp = temp.father;
		}
		return temp.father2thisAct;
	}
	
	public boolean getKey(StateObservation stateObs) {//到达key原本所在的位置
		Vector2d avatorpos = stateObs.getAvatarPosition();
		return avatorpos.equals(orikeypos);
	}

	public double heuristic(StateObservation stateObs) {
		double result = 0;
		Vector2d avatorpos = stateObs.getAvatarPosition();//精灵的坐标
		ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
		ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
		Vector2d goalpos = fixedPositions[fixedPositions.length-1].get(0).position; //目标的坐标
		//System.out.println(goalpos);
		//System.out.println(avatorpos);
		Vector2d keypos; //钥匙的坐标
		if(!getkey) {
			if(getKey(stateObs)&&movingPositions[0].size()==0){//这一步取得钥匙{
				result=0;
			}
			else {//先去找钥匙
				keypos = movingPositions[0].get(0).position;
				double x_diff = Math.abs(avatorpos.x-keypos.x);
				double y_diff = Math.abs(avatorpos.y-keypos.y);
				result = x_diff+y_diff;	
			}
		}	
		else if(getkey) {//拿到钥匙向回走
			double x_diff = Math.abs(avatorpos.x-goalpos.x);
			double y_diff = Math.abs(avatorpos.y-goalpos.y);
			result = x_diff+y_diff;
		}
		return result-stateObs.getGameScore();
	}
}


