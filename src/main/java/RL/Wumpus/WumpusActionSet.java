package RL.Wumpus;

public class WumpusActionSet {
	
	public static final int WAKE_UP = 0;
	public static final int MOVE_RIGHT = 1;
	public static final int MOVE_LEFT = 2;
	public static final int MOVE_UP = 3;
	public static final int MOVE_DOWN = 4;
	public static final int NO_OP = 5;
	public static final int SELF_TERMINATE = 6;

	public static String getName(int actionCode) {
		switch (actionCode) {
			case WAKE_UP: return "WAKE_UP";
			case MOVE_RIGHT: return "MOVE_RIGHT";
			case MOVE_LEFT: return "MOVE_LEFT";
			case MOVE_UP: return "MOVE_UP";
			case MOVE_DOWN: return "MOVE_DOWN";
			case NO_OP: return "NO_OP";
			case SELF_TERMINATE: return "SELF_TERMINATE";
			default: return "@";
		}
	}
	
}