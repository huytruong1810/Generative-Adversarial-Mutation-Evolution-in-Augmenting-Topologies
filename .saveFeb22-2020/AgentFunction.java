import java.util.Random;

class AgentFunction {
	
	// the action table
	private int[] actionTable;
	
	// the knowledge base
	private String[] KB;
	
	private Random rand;

	public AgentFunction(int worldSizeKnowledge)
	{

		// this integer array will store the agent actions
		actionTable = new int[8];
				  
		actionTable[0] = ActionSet.MOVE_FORWARD;
		actionTable[1] = ActionSet.MOVE_FORWARD;
		actionTable[2] = ActionSet.MOVE_FORWARD;
		actionTable[3] = ActionSet.MOVE_FORWARD;
		actionTable[4] = ActionSet.TURN_RIGHT;
		actionTable[5] = ActionSet.TURN_LEFT;
		actionTable[6] = ActionSet.GRAB;
		actionTable[7] = ActionSet.SHOOT;
		
		KB = new String[4];
		
		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();
	}
	
	// map from the perceived data to the action that needs to be taken
	public int process (InnerPerceivedData P)
	{
		
		// save in the current percepts for convenience
		boolean z = P.IFeelFF();
		boolean g = P.ISeeGlitter();
		boolean b = P.IFeelBreeze();
		boolean st = P.ISmellStench();
		boolean sc = P.IHearScream();
		
		
		
		// return action to be performed
	    return actionTable[rand.nextInt(8)];
	    
	}
	
}