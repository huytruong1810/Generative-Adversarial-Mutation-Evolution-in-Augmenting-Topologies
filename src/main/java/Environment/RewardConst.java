package Environment;

public class RewardConst {

    // reward for all agents
    public static final int bumpCost = -5;
    public static final int actionCost = -1;

    // human reward shaping
    public static final int goldReward = 700;
    public static final int missGrabCost = -10;
    public static final int deathCost = -50;
    public static final int shootCost = -5;

    // wumpus reward shaping
    public static final int killReward = 1000;
    public static final int lostCost = -1000;

}
