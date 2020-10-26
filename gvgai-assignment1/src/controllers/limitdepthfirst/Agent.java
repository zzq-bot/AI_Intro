package controllers.limitdepthfirst;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractPlayer {
	public static boolean tag; 
	public ArrayList<StateObservation> visited=new ArrayList<StateObservation>();//历史路径
	public ArrayList<Integer> visitedDepth = new ArrayList<Integer>();//历史路径对应层数 
	public ArrayList<Types.ACTIONS> actions=new ArrayList<Types.ACTIONS>();//存储成功路径所需每一步动作
	public static int depthLevel;//搜索深度
	public static double bestScore;//当前局面启发式最佳评分
	public Types.ACTIONS finalAct;//最终采取行动
	Vector2d orikeypos; //钥匙的坐标
	public boolean getkey;//是否已经取到钥匙
	public ArrayList<StateObservation> wholeVisited=new ArrayList<StateObservation>();//真实执行历史路径
	
	
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer){
		ArrayList<Observation>[] movingPositions = so.getMovablePositions();
		orikeypos = movingPositions[0].get(0).position;
		getkey = false;
	 }
	
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		initVar();
		StateObservation stCopy = stateObs.copy();
		wholeVisited.add(stCopy);
		if(getKey(stateObs)) {
			getkey = true;
		}
		
		Types.ACTIONS action = null;
		tag = recursiveDFS(stateObs,elapsedTimer,0);
		action = finalAct;
		return action;
	}
	public void initVar() {
		visited.clear();
		visitedDepth.clear();
		actions.clear();
		depthLevel = 3;
		tag = false;
		bestScore = 1000000;
	}
	public boolean recursiveDFS(StateObservation stateObs, ElapsedCpuTimer elapsedTimer,int depth) {
		if(depth!=0&&isVisited(stateObs,depth)) {
			return false;
		}
		if(depth!=0&&isWholeVisited(stateObs)) {
			return false;
		}
		visited.add(stateObs);
		visitedDepth.add(depth);
		if(stateObs.getGameWinner()==Types.WINNER.PLAYER_LOSES) {//如果游戏失败
			return false;
		}
		if(stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS) {
			bestScore = depth;
			finalAct = actions.get(0);
			return true;
		}
		if(getKey(stateObs)&&depth!=0&&!getkey) {
			if(bestScore>depth) {
				bestScore = depth;
				finalAct = actions.get(0);
				return true;
			}
		}
	    if(depth==depthLevel||stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS) {
			double score = heuristic(stateObs);
			if(score<bestScore) {//如果当前行动更优
				bestScore = score;
				finalAct = actions.get(0);
				return true;
			}
			else
				return false;
		}
		else {
			StateObservation stCopy = stateObs.copy();
			ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions();	
			for(int i=0;i<availableActions.size();i++) {
				if(depth==0) {
					actions.clear();
				}
				stCopy = stateObs.copy();
				Types.ACTIONS thisact = availableActions.get(i);
				actions.add(thisact);
				stCopy.advance(thisact);
				if(!recursiveDFS(stCopy,elapsedTimer,depth+1)) {
					actions.remove(actions.size()-1);
					stCopy = stateObs.copy();
					continue;
				}
				else {
					continue;
				}
			}
		}
		return true;
	}
	
	public boolean isVisited(StateObservation stateObs,int depth) {
		for(int i=0;i<visited.size();i++) {
			if(stateObs.equalPosition(visited.get(i))&&depth>=visitedDepth.get(i)) {
				return true;
			}
		}
		return false;
	}
	public boolean isWholeVisited(StateObservation stateObs) {
		for(int i=0;i<wholeVisited.size();i++) {
			if(stateObs.equalPosition(wholeVisited.get(i))) {
				return true;
			}
		}
		return false;
	}

	
	public double heuristic(StateObservation stateObs) {
		double result = 0;
		Vector2d avatorpos = stateObs.getAvatarPosition();//精灵的坐标
		ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
		ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
		Vector2d goalpos = fixedPositions[fixedPositions.length-1].get(0).position; //目标的坐标
		Vector2d keypos; //钥匙的坐标
		
		if(getKey(stateObs)&&movingPositions[0].size()==0){//这一步取得钥匙{
			return 0;
			}
		else if(getkey) {//拿到钥匙向回走
			double x_diff = Math.abs(avatorpos.x-goalpos.x);
			double y_diff = Math.abs(avatorpos.y-goalpos.y);
			result = x_diff+y_diff;
		}
		else {//先去找钥匙
			keypos = movingPositions[0].get(0).position;
			double x_diff = Math.abs(avatorpos.x-keypos.x);
			double y_diff = Math.abs(avatorpos.y-keypos.y);
			result = x_diff+y_diff;
		}
		return result;
	}
	
	public boolean getKey(StateObservation stateObs) {//到达key原本所在的位置
		Vector2d avatorpos = stateObs.getAvatarPosition();
		return avatorpos.equals(orikeypos);
	}
	
}