
import core.ArcadeMachine;
import java.util.Random;
import core.competition.CompetitionParameters;

public class test2 {
	public static void main(String[] args)
    {
    	String limitdepthfirstController = "controllers.limitdepthfirst.Agent";
 

        boolean visuals = true; // set to false if you don't want to see the game
        int seed = new Random().nextInt(); // seed for random
       
        
        
        /****** Task 2 ******/
        CompetitionParameters.ACTION_TIME = 100; // no time for finding the whole path
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl0.txt", true, limitdepthfirstController, null, seed, false);
        
       
    }   
}
