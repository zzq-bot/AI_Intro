
import core.ArcadeMachine;
import java.util.Random;
import core.competition.CompetitionParameters;

public class test3 {
 
    public static void main(String[] args)
    {
        //Available controllers:
        String AstarController = "controllers.Astar.Agent";

        boolean visuals = true; // set to false if you don't want to see the game
        int seed = new Random().nextInt(); // seed for random
     
        /****** Task 3 ******/
        CompetitionParameters.ACTION_TIME = 500; // no time for finding the whole path
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl0.txt", true, AstarController, null, seed, false);
        ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl1.txt", true, AstarController, null, seed, false);
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl2.txt", true, AstarController, null, seed, false);
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl3.txt", true, AstarController, null, seed, false);
        //ArcadeMachine.runOneGame("examples/gridphysics/bait.txt", "examples/gridphysics/bait_lvl4.txt", true, AstarController, null, seed, false);
    
        
    }   
}