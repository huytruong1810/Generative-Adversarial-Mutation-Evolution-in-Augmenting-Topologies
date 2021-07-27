package Environment;

public class RewardSystem {

    // reward for all agents
    public static final int bumpCost = -1;
    public static final int actionCost = -1;
    public static final int timeCost = -5;

    // human reward shaping
    public static final int goldReward = 10;
    public static final int missGrabCost = -2;
    public static final int deathCost = -10;
    public static final int shootCost = -5;

    // wumpus reward shaping
    public static final int killReward = 10;
    public static final int lostCost = -10;

}
