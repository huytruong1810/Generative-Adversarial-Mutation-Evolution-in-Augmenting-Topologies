package Human;

import Environment.Environment;

public class HumanSensory {
	
	private final Environment E;
		
	public HumanSensory(Environment e) { E = e; }

	public double[] produceState(int takenAction, int numPercept) {
		double[] stateVec = new double[numPercept];
		stateVec[0] = (E.thereIsHWall()) ? 1 : -1;
		stateVec[1] = (E.humanSeeGlitter()) ? 1 : -1;
		stateVec[2] = (E.humanFeelBreeze()) ? 1 : -1;
		stateVec[3] = (E.humanSmellStench()) ? 1 : -1;
		stateVec[4] = (E.humanHearScream()) ? 1 : -1;
		stateVec[5] = takenAction * 0.1 + 1; // normalize action value
		return stateVec;
	}
	
}