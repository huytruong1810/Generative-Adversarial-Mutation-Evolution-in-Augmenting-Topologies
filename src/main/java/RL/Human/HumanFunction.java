package RL.Human;

import NEAT.DataStructures.MemUnit;
import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.DataStructures.MemStream;
import NEAT.Individual;

import java.util.HashMap;

import static NEAT.NEAT.*;

public class HumanFunction {

	public static final int[] actionTable = new int[] {
			HumanActionSet.MOVE_FORWARD,
			HumanActionSet.TURN_RIGHT,
			HumanActionSet.TURN_LEFT,
			HumanActionSet.GRAB,
			HumanActionSet.SHOOT,
			HumanActionSet.NO_OP
	};

	private final boolean inTesting;
	private final int codeLength;
	private final MHg MH;
	private final DHg DH;
	private final HumanSensory senses;

	// these variable's values are only relevant during a complete train/test episode
	private int takenAction;
	private double[] probs, nextStatePred;
	private double V_s;
	private final MemStream memory;

	// for graphical components, must only be collected when most updated
	public double[] getProbs() { return probs; } // most updated right after process()
	public double getStateValue() { return V_s; } // most updated right after process()
	public double[] getNextStatePred() { return nextStatePred; } // most updated right after process()

	public HumanFunction(Individual i, HumanSensory sensorySystem, boolean testing) {

		inTesting = testing;
		takenAction = HumanActionSet.WAKE_UP; // first thing is waking up

		// extract genome
		// NOTE that a new human/brain is init every train/test episode but genome is altered permanently
		MH = i.getMH();
		codeLength = MH.getOutputNum();
		DH = i.getDH();

		// here signifies the begin of an episode
		MH.episodePrep();
		DH.episodePrep();

		// keep reference to the sensory system
		senses = sensorySystem;

		// initialize internal memory stack with size of a unit
		memory = new MemStream(codeLength * 2);

	}

	/**
	 * Processes the senses and decides what action to take
	 * IMPORTANT: Each process() needs to be followed by a logEnvironmentReturn()
	 * @param dead - is the agent dead
	 * @return action decision
	 */
	public int process(boolean dead) {

		// get actor's action distribution at time t with previously selected action and previous V(s)
		MemUnit actorUnit = MH.feed(memory.recent(), senses.produceActorState(obsII.length, takenAction, V_s), 'a');
	    probs = DH.feed(actorUnit.t(), actorUnit.h('a'), 'a');

		int selectedActionIndex = IMPOSSIBLE_VAL;
		if (dead) { // the only case where taken action is not based on selected action-index
			selectedActionIndex = 5; // index 5 is no-op so stalling will get a bad reputation
		}
		else { // stochastic action selection which includes exploration
			double p = Math.random(), cumulative = 0.0;
			for (int i = 0, n = probs.length; i < n; ++i) {
				cumulative += probs[i];
				if (p < cumulative) { selectedActionIndex = i; break; }
			}
		}
		takenAction = actionTable[selectedActionIndex]; // taken action is updated

		// get critic's V(s) at time t with current selected action and previous V(s)
		MemUnit criticUnit = MH.feed(memory.recent(), senses.produceCriticState(obsII.length, takenAction, V_s), 'c');
		V_s = DH.feed(criticUnit.t(), criticUnit.h('c'), 'c')[0];

		// get seer's next state prediction at time t with current selected action and current V(s)
		MemUnit seerUnit = MH.feed(memory.recent(), senses.produceSeerState(obsII.length, takenAction, V_s), 's');
		nextStatePred = DH.feed(seerUnit.t(), seerUnit.h('s'), 's');

		// complete the actor unit with critic's and seer's so we can update memory
		actorUnit.complete(criticUnit, seerUnit);
		memory.push(actorUnit);

		memory.recent().setOutputs(selectedActionIndex, V_s, probs, nextStatePred);

		return takenAction;
	    
	}

	/**
	 * NOTE: must only be called after equivalent process() because process push the most recent
	 * timeunit onto the memory stream
	 * Logs the environment returned reward after an action is taken
	 * @param reward - the returned reward
	 */
	public void logReward(double reward) {
		// consider reward that promotes intrinsic curiosity
		int obsLen = obsII.length;
		double[] newState = senses.produceSeerState(obsLen, 0, 0); // only care about observations
		double surprise = 0;
		for (int i = 0; i < obsLen; ++i) surprise += Math.abs(newState[i] - nextStatePred[i]);
		reward += 1e-2 * surprise / obsLen;
		// save the reward for learning
		memory.recent().set_r(reward);
	}

	/**
	 * NOTE: must only be called after each episode ends
	 * Tunes the DH and MH at the end of an episode
	 */
	public void adjust() {

		if (inTesting) throw new IllegalStateException("Adjust should not happen during testing.");

		double[] dL_dCnext = new double[codeLength * 3]; // store for all actor-critic-seer
		double[] dL_dhnext = new double[codeLength * 3];

		memory.shave(); // shave off [t = -1] unit and fix max time
		int t = memory.maxTime(); // the latest time step

		while (t >= 0) { // done when see a negative time step

			// for collecting gradient returns of ACU and training MRU
			HashMap<Integer, Double> actorGradRet = new HashMap<>();
			HashMap<Integer, Double> criticGradRet = new HashMap<>();
			HashMap<Integer, Double> seerGradRet = new HashMap<>();
			// train decision head's parameters
			DH.train(t, LAMBDA, GAMMA, memory, actorGradRet, criticGradRet, seerGradRet);
			// train memory head's gates using back gradients of decision head
			// dL_dCnext and dL_dhnext are updated overtime
			MH.train(t, memory, dL_dCnext, dL_dhnext, actorGradRet, criticGradRet, seerGradRet);

			--t; // go to previous time step

		}

		// here signifies the end of the episode
		memory.clear();
		DH.episodeDone();
		MH.episodeDone();

	}
	
}