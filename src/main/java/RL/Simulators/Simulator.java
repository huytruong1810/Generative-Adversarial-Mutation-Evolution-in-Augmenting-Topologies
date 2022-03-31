package RL.Simulators;

import RL.Environment;
import RL.Human.Human;
import RL.Human.HumanActionSet;
import NEAT.Individual;
import RL.Wumpus.Wumpus;
import RL.Wumpus.WumpusActionSet;

public abstract class Simulator {

    // reward for all agents
    protected static final double bumpCost = -0.005;
    protected static final double actionCost = -0.001;

    // human-specific reward assignment
    protected static final double goldReward = 0.7;
    protected static final double missGrabCost = -0.01;
    protected static final double deathCost = -0.05;
    protected static final double shootCost = -0.005;

    // wumpus-specific reward assignment
    protected static final double killReward = 0.1;
    protected static final double lostCost = -0.1;

    // human attributes
    protected Human human;
    protected double hScore;
    protected int hTakenAction;

    // wumpus attributes
    protected Wumpus wumpus;
    protected double wScore;
    protected int wTakenAction;

    // simulation attributes
    protected int timeSteps;
    protected int worldSize;
    protected int stepCounter;
    protected Environment environment;

    public double getHScore() { return hScore; }
    public double getWScore() { return wScore; }
    public int getStepCounter() { return stepCounter; }
    public Environment getEnvironment() { return environment; }
    public String getHAction() { return HumanActionSet.getName(hTakenAction); }
    public String getWAction() { return WumpusActionSet.getName(wTakenAction); }

    public Simulator(int s, int t) {
        worldSize = s; // for setting up the environment
        timeSteps = t; // for checking world lifetime
    }

    /**
     * Reset the simulation
     * IMPORTANT:
     *      * - for training, inputs have to be the same individuals because their parameters
     *      *   will be updated throughout the training phase
     *      * - for testing, inputs can be clones because their parameters won't be updated and
     *      *   it will support threading
     *      * - for bagging, environment only needs to be built once while multiple humans
     *      *   need to be added
     * @param bp - the blueprint for the env
     * @param h - the human intelligence
     * @param w - the wumpus intelligence
     */
    public void reset(char[][][] bp, Individual h, Individual w) {
        stepCounter = 1;
        hScore = wScore = 0;
        hTakenAction = HumanActionSet.WAKE_UP;
        wTakenAction = WumpusActionSet.WAKE_UP;
        resetModel(bp, h, w);
    }

    public abstract void resetModel(char[][][] bp, Individual h, Individual w);

    /**
     * Progress the environment by one time step
     * IMPORTANT:
     *      * - for training, one step involves perceiving, acting, and learning
     *      * - for testing and bagging, one step involves perceiving and acting
     * @return status after step:
     *  -2: time's up
     *  -1: human dies
     *  0: everything is good
     *  1: human wins
     *  2: wumpus wins
     */
    public abstract int step();

    protected double actuateHAction(int action) {

        double reward = 0;
        switch (action) {
            case HumanActionSet.MOVE_FORWARD: reward += actionCost;
                if (!human.moveForward()) { reward += bumpCost;
                    environment.setHWall(true);
                }
                break;
            case HumanActionSet.TURN_RIGHT: reward += actionCost;
                human.turnRight();
                break;
            case HumanActionSet.TURN_LEFT: reward += actionCost;
                human.turnLeft();
                break;
            case HumanActionSet.GRAB: reward += actionCost;
                if (environment.goldIsThereToGrab()) human.takeGold();
                else reward += missGrabCost;
                break;
            case HumanActionSet.SHOOT: reward += actionCost;
                if (human.shoot()) { reward += shootCost;
                    if (environment.arrowHit()) {
                        environment.setScream(true);
                        wumpus.terminate();
                    }
                }
                break;
            case HumanActionSet.NO_OP:
                break; // do nothing
        }
        return reward;

    }

    protected double actuateWAction(int action) {

        double reward = 0;
        boolean bumped = false;
        switch (action) {
            case WumpusActionSet.MOVE_RIGHT: reward += actionCost;
                if (!wumpus.moveRight()) bumped = true;
                break;
            case WumpusActionSet.MOVE_LEFT: reward += actionCost;
                if (!wumpus.moveLeft()) bumped = true;
                break;
            case WumpusActionSet.MOVE_UP: reward += actionCost;
                if (!wumpus.moveUp()) bumped = true;
                break;
            case WumpusActionSet.MOVE_DOWN: reward += actionCost;
                if (!wumpus.moveDown()) bumped = true;
                break;
            case WumpusActionSet.NO_OP: // do nothing
                break;
        }
        if (bumped) { reward += bumpCost;
            environment.setWWall(true);
        }
        return reward;

    }

}
