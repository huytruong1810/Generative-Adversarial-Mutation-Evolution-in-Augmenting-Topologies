package RL.Human;

public class HumanActionSet { // do not use enum because complicated factoring of int values

	public static final int WAKE_UP = -3;
	public static final int MOVE_FORWARD = -2;
	public static final int TURN_RIGHT = -1;
	public static final int TURN_LEFT = 0;
	public static final int GRAB = 1;
	public static final int SHOOT = 2;
	public static final int NO_OP = 3;
	public static final int SELF_TERMINATE = 4;

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