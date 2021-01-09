package Human;

import Neat.Genome.Genome;

import static Neat.Neat.GAMMA;

public class HumanFunction {

	private final int numPercept, numAction;
	private final int[] actionTable;
	private Genome intelligence;
	private double[] probs, input;
	private double V_s, TDTarget;

	public double[] getProbs() { return probs; }
	public double getStateValue() { return V_s; }
	public double getTDTarget() { return TDTarget; }

	public HumanFunction(Genome g) {

		numPercept = numAction = 5;
		actionTable = new int[numAction];
		actionTable[0] = HumanActionSet.MOVE_FORWARD;
		actionTable[1] = HumanActionSet.TURN_RIGHT;
		actionTable[2] = HumanActionSet.TURN_LEFT;
		actionTable[3] = HumanActionSet.GRAB;
		actionTable[4] = HumanActionSet.SHOOT;
		intelligence = g;

	}

	private double[] produceState(HumanSensory senses) {
		double[] stateVec = new double[numPercept];
		if (senses.IFeelBump()) stateVec[0] = 1;
		if (senses.ISeeGlitter()) stateVec[1] = 1;
		if (senses.IFeelBreeze()) stateVec[2] = 1;
		if (senses.ISmellStench()) stateVec[3] = 1;
		if (senses.IHearScream()) stateVec[4] = 1;
		return stateVec;
	}

	public int process(HumanSensory senses) {

		// cache the input for later training
		input = produceState(senses);

		// get actor's action opinion
	    probs = intelligence.feed(input, true);

	    // stochastic action selection
		double num = Math.random(), c = 0;
		for (int i = 0; i < numAction; ++i) { // only first half is result of softmax
			c += probs[i];
			if (num < c) return actionTable[i];
		}
		throw new IllegalStateException("Stochastic action selection fails.");
	    
	}

	public void adjust(HumanSensory senses, int reward, int takenAction) {

		int c = -1; // find correct action index c of taken action
		for (int i = 0; i < numAction; ++i) if (actionTable[i] == takenAction) { c = i; break; }

		// get critic's state-value opinion
		V_s = intelligence.feed(input, false)[0];
		// bootstrap true state-value using TD(0) target
		TDTarget = reward + GAMMA * intelligence.feed(produceState(senses), false)[0];
		// train critic using MSE gradient (true - pred)
		intelligence.train(input, TDTarget - V_s, c, false);
		// train actor using critic's opinion and Advantage Function
		intelligence.train(input, (TDTarget - V_s) / probs[c], c, true);

	}
	
}