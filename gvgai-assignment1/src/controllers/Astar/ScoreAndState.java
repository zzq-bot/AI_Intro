package controllers.Astar;

import core.game.StateObservation;
import ontology.Types;

public class ScoreAndState {
	public StateObservation stateObs;
	public double score;
	public double gScore;
	public double hScore;
	public ScoreAndState father;
	public Types.ACTIONS father2thisAct;
	public ScoreAndState(StateObservation stateObs,double gScore,double hScore,ScoreAndState father) {
		this.stateObs = stateObs.copy();
		this.gScore = gScore;
		this.hScore = hScore;
		this.score = gScore+hScore;
		this.father = father;
		this.father2thisAct = null;
	}
}
