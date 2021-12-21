package Human;

import Environment.Environment;

public class HumanSensory {
	
	private final Environment E;
		
	public HumanSensory(Environment e) { E = e; }

	private void collectObs(double[] featVec) {
		// 5 observations from environment
		featVec[0] = (E.thereIsHWall()) ? 1 : -1;
		featVec[1] = (E.humanSeeGlitter()) ? 1 : -1;
		featVec[2] = (E.humanFeelBreeze()) ? 1 : -1;
		featVec[3] = (E.humanSmellStench()) ? 1 : -1;
		featVec[4] = (E.humanHearScream()) ? 1 : -1;
	}

	public double[] produceActorState(int obsLength, double prevAction, double prevStateValue) {

		double[] featVec = new double[obsLength + 2];

		collectObs(featVec);

		featVec[obsLength] = prevAction;
		featVec[obsLength + 1] = prevStateValue;

		return featVec;

	}

	public double[] produceCriticState(int obsLength, double action, double prevStateValue) {

		double[] featVec = new double[obsLength + 2];

		collectObs(featVec);

		featVec[obsLength] = action;
		featVec[obsLength + 1] = prevStateValue;

		return featVec;

	}

	public double[] produceSeerState(int obsLength, double action, double stateValue) {

		double[] featVec = new double[obsLength + 2];

		collectObs(featVec);

		featVec[obsLength] = action;
		featVec[obsLength + 1] = stateValue;

		return featVec;

	}
	
}