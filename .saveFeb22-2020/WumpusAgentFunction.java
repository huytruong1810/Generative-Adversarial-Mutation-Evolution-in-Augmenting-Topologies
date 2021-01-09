import java.util.Random;

class WumpusAgentFunction {
	
	// the action table
	private int[] actionTable;
	
	private Random rand;

	public WumpusAgentFunction()
	{

		// this integer array will store the agent actions
		actionTable = new int[7];
				  
		actionTable[0] = WumpusActionSet.MOVE_FORWARD;
		actionTable[1] = WumpusActionSet.MOVE_FORWARD;
		actionTable[2] = WumpusActionSet.MOVE_FORWARD;
		actionTable[3] = WumpusActionSet.MOVE_FORWARD;
		actionTable[4] = WumpusActionSet.TURN_RIGHT;
		actionTable[5] = WumpusActionSet.TURN_LEFT;
		actionTable[6] = WumpusActionSet.SLAM;
		
		// new random number generator, for
		// randomly picking actions to execute
		rand = new Random();
	}
	
	// map from the perceived data to the action that needs to be taken
	public int process (WumpusInnerPerceivedData WP)
	{
		
		// save in the current percepts for convenience
		boolean z = WP.IFeelFF();
		boolean b = WP.IFeelBreeze();
		boolean h = WP.ISmellHuman();
		
		if (z)
			return actionTable[4];
		// noted that if the wumpus and agent moving into the same space
		// at the same time but the agent then proceed to leave the space,
		// the wumpus will not have enough time to react since the wumpus can
		// only perceive humany effect when it is with the agent
		else if (h)
			return actionTable[6];
		else {
			// do something
		}
		
		// return action to be performed
	    return actionTable[rand.nextInt(7)];
	    
	}
	
}