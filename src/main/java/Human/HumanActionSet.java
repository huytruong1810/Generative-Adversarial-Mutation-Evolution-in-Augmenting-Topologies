package Human;

public class HumanActionSet {
	
	public static final int WAKE_UP = 0;
	public static final int MOVE_FORWARD = 1;
	public static final int TURN_RIGHT = 2;
	public static final int TURN_LEFT = 3;
	public static final int GRAB = 4;
	public static final int SHOOT = 5;
	public static final int NO_OP = 6;
	public static final int SELF_TERMINATE = 7;

	public static String getName(int actionCode) {
		switch (actionCode) {
			case WAKE_UP: return "WAKE_UP";
			case MOVE_FORWARD: return "MOVE_FORWARD";
			case TURN_RIGHT: return "TURN_RIGHT";
			case TURN_LEFT: return "TURN_LEFT";
			case GRAB: return "GRAB";
			case SHOOT: return "SHOOT";
			case NO_OP: return "NO_OP";
			case SELF_TERMINATE: return "SELF_TERMINATE";
			default: return "@";
		}
	}
	
}