// this class stores all actions possible to the wumpus
// and the number that represents each action so it is
// more convenient to keep track of
class WumpusActionSet {
	
	public static int WAKE_UP = 0;
	public static int MOVE_FORWARD = 1;
	public static int TURN_RIGHT = 2;
	public static int TURN_LEFT = 3;
	public static int SLAM = 4;
	public static int NO_OP = 5;
	public static int SELF_TERMINATE = 6;
	
	public WumpusActionSet() {
		
		// nothing to construct...
		
	}

	public static String printAction(int action) {
		
		if (action == 0) return "WAKE_UP";
		else if (action == 1) return "MOVE_FORWARD";
		else if (action == 2) return "TURN_RIGHT";
		else if (action == 3) return "TURN_LEFT";
		else if (action == 4) return "SLAM";
		else if (action == 5) return "NO_OP";
		else return "SELF_TERMINATE";
		
	}
	
}