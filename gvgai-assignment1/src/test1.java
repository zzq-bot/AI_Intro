
import core.ArcadeMachine;
import java.util.Random;
import core.competition.CompetitionParameters;

public class test1 {
	 
    public static void main(String[] args)
    {
        //Available controllers:
    	String depthfirstController = "controllers.depthfirst.Agent";

        boolean visuals = true; // set to false if you don't want to see the game
        int seed = new Random().nextInt(); // seed for random
        
        
        /****** Task 1 ******/
        CompetitionParameters.ACTION_TIME = 10000; // set to the time that allow you to do the depth first search
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl2.txt", true, depthfirstController, null, seed, false);
        
   
   
        
    }   
}