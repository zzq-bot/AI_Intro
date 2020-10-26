package controllers.depthfirst;

import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import core.player.AbstractPlayer;

public class Agent extends AbstractPlayer {
	public static boolean tag; //
	public ArrayList<StateObservation> visited=new ArrayList<StateObservation>();//历史路径
	public ArrayList<Types.ACTIONS> actions=new ArrayList<Types.ACTIONS>();//存储成功路径所需每一步动作

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer){
		tag = false;
	 }
	
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		Types.ACTIONS action = null;
		if(actions.size()==0) {	//如果actions有数据说明已经被执行过了,直接将action中的元素一个个输出
			tag = getDFS(stateObs,elapsedTimer); //DFS
		}
		if(tag) {
			action = actions.remove(actions.size()-1);
		}
		return action;
	}
	
	public boolean getDFS(StateObservation stateObs,ElapsedCpuTimer elapsedTimer) {
		if(is_Visited(stateObs)) { // 如果已经走过
			return false;
		}
		if(stateObs.getGameWinner()==Types.WINNER.PLAYER_WINS) {// 如果游戏胜利
			return true;
		}
		if(stateObs.getGameWinner()==Types.WINNER.PLAYER_LOSES) {//如果游戏失败
			return false;
		}
		visited.add(stateObs);
		StateObservation stCopy = stateObs.copy();
		ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions();	
		for(int i=0;i<availableActions.size();i++) {
			stCopy = stateObs.copy();
			Types.ACTIONS thisact = availableActions.get(i);
			stCopy.advance(thisact);
			if(getDFS(stCopy,elapsedTimer)) {
				actions.add(thisact);
				return true;
			}
			else {
				stCopy = stateObs.copy();//reset stCopy
				continue;
			}
		}
		return false;
	}
	
	public boolean is_Visited(StateObservation stateObs) {
		for(int i=0;i<visited.size();i++) {
			if(stateObs.equalPosition(visited.get(i))) {
				return true;
			}
		}
		return false;
	}
}