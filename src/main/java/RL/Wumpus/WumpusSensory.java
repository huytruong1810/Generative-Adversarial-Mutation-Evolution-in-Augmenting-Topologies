package RL.Wumpus;

import RL.Environment;

public class WumpusSensory {
	
	private final Environment E;
		
	public WumpusSensory(Environment e) { E = e; }

	public double[] produceState(int takenAction, int numPercept) {
		double[] stateVec = new double[numPercept];
		stateVec[0] = (E.thereIsWWall()) ? 1 : -1;
		stateVec[1] = (E.wumpusSmellScent()) ? 1 : -1;
		stateVec[2] = E.wumpusSmellDirectionIs();
		stateVec[3] = E.wumpusSmellIntensityIs();
		stateVec[4] = takenAction * 0.1 + 1; // normalize action value
		return stateVec;
	}
	
}