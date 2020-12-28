package controllers.Heuristics;

import java.util.ArrayList;
import java.util.LinkedList;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:44
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WinScoreHeuristic extends StateHeuristic {

    private static final double HUGE_NEGATIVE = -1000.0;
    private static final double HUGE_POSITIVE =  1000.0;

    double initialNpcCounter = 0;

    public WinScoreHeuristic(StateObservation stateObs) {

    }

    public double evaluateState(StateObservation stateObs) {
        boolean gameOver = stateObs.isGameOver();
        Types.WINNER win = stateObs.getGameWinner();
        double rawScore = 30*stateObs.getGameScore();

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            return HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            return HUGE_POSITIVE;
        
        
        Vector2d avatar = stateObs.getAvatarPosition();
        int avatar_x = (int)avatar.x / 28;
        int avatar_y = (int)avatar.y / 28;
        //生命值
        int []y_wedge = new int[50];
        int y_wedge_cnt = 0;
        rawScore+=20*stateObs.getAvatarHealthPoints();
        
        LinkedList<Observation> allobj = new LinkedList<>();
        if(stateObs.getPortalsPositions()!=null)
        	for(ArrayList<Observation> l:stateObs.getPortalsPositions())	allobj.addAll(l);
        if( stateObs.getImmovablePositions()!=null )
            for(ArrayList<Observation> l : stateObs.getImmovablePositions()) allobj.addAll(l);
        //到Portal距离
        for(Observation o :allobj) {
        	Vector2d p = o.position;
            int x = (int)(p.x/28); 
            int y= (int)(p.y/28); 
        	if (o.itype==4) { 
                int xdis2portal = Math.abs(x-avatar_x);
            	int ydis2portal = Math.abs(y-avatar_y);
            	rawScore+=(150-15*ydis2portal);
            	if(ydis2portal==0) {
            		rawScore+=100-5*xdis2portal;
            	}
            	else {
            		rawScore+=30-xdis2portal;
            	}
        	}
        	if(o.itype==8||o.itype==7||o.itype==10||o.itype==11) {
        		y_wedge[y_wedge_cnt] = y;
        		y_wedge_cnt++;
        	}
        }
        //是否在安全行
        boolean flag = true;
        for(int i=0;i<y_wedge_cnt;i++) {
        	if(y_wedge[i]==avatar_y) {
        		flag = false;
        		break;
        	}
        }
       
        if(flag&&avatar_y!=1)	rawScore+=20;
        else	rawScore-=20;
        return rawScore;
    }
}

