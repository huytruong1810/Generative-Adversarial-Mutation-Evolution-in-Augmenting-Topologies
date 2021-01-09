// this class stores all actions possible to the agent
// and the number that represents each action so it is
// more convenient to keep track of
class ActionSet {
	
	public static int WAKE_UP = 0;
	public static int MOVE_FORWARD = 1;
	public static int TURN_RIGHT = 2;
	public static int TURN_LEFT = 3;
	public static int GRAB = 4;
	public static int SHOOT = 5;
	public static int NO_OP = 6;
	public static int SELF_TERMINATE = 7;
	
	public ActionSet() {
		
		// nothing to construct...
		
	}

	public static String printAction(int action) {
		
		if (action == 0) return "WAKE_UP";
		else if (action == 1) return "MOVE_FORWARD";
		else if (action == 2) return "TURN_RIGHT";
		else if (action == 3) return "TURN_LEFT";
		else if (action == 4) return "GRAB";
		else if (action == 5) return "SHOOT";
		else if (action == 6) return "NO_OP";
		else return "SELF_TERMINATE";
		
	}
	
}