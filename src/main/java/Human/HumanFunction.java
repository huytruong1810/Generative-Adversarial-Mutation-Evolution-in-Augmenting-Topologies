package Human;

import java.util.Random;

public class HumanFunction {

	private int[] actionTable;
	private Random rand;

	public HumanFunction() {

		actionTable = new int[8];
		actionTable[0] = HumanActionSet.MOVE_FORWARD;
		actionTable[1] = HumanActionSet.MOVE_FORWARD;
		actionTable[2] = HumanActionSet.MOVE_FORWARD;
		actionTable[3] = HumanActionSet.MOVE_FORWARD;
		actionTable[4] = HumanActionSet.TURN_RIGHT;
		actionTable[5] = HumanActionSet.TURN_LEFT;
		actionTable[6] = HumanActionSet.GRAB;
		actionTable[7] = HumanActionSet.SHOOT;
		rand = new Random();

	}
	
	// map from the perceived data to the action that needs to be taken
	public int process(HumanSensory senses) {

		if (senses.ISeeGlitter())
			return actionTable[6];

		if (senses.IFeelBump())
			return actionTable[4];

	    return actionTable[rand.nextInt(8)];
	    
	}
	
}