package Human;

import NEAT.Genome.ACU.ACUConGene;
import NEAT.Genome.ACU.ACUGenome;
import NEAT.Genome.MRU.MRUConGene;
import NEAT.Genome.MRU.MRUGenome;
import NEAT.DataStructures.MemStack;
import NEAT.DataStructures.MemUnit;
import NEAT.Individual;

import java.util.HashMap;

import static NEAT.NEAT.*;

public class HumanFunction {

	private final int numPercept, numAction;
	private final boolean inTesting;
	private final int[] actionTable;
	private final MRUGenome MRU;
	private final ACUGenome ACU;

	// these components are only relevant during a complete simulation
	private int takenAction;
	private double[] probs;
	private double V_s, TDTarget, explorationRate;
	private final MemStack memory;

	// for graphical components, must only be collected when most updated
	public double[] getProbs() { return probs; } // most updated right after process()
	public double getStateValue() { return V_s; } // most updated right after process()
	public double getTDTarget() { return TDTarget; } // most updated right after logEnvironmentReturn()

	public HumanFunction(Individual i, boolean testing) {

		inTesting = testing;
		takenAction = HumanActionSet.WAKE_UP; // first thing is waking up
		explorationRate = IER;
		numPercept = 6;
		numAction = 5;
		actionTable = new int[numAction]; // the available actions
		actionTable[0] = HumanActionSet.MOVE_FORWARD;
		actionTable[1] = HumanActionSet.TURN_RIGHT;
		actionTable[2] = HumanActionSet.TURN_LEFT;
		actionTable[3] = HumanActionSet.GRAB;
		actionTable[4] = HumanActionSet.SHOOT;

		MRU = i.getMRU();
		ACU = i.getACU();

		memory = new MemStack();
		memory.push(new MemUnit(-1, null,
				new double[MRU.getOutputNum()], // initialize hidden state to zero vector
				new double[MRU.getOutputNum()], // initialize cell state to zero vector
				null));

	}

	/**
	 * Processes the senses and decides what action to take
	 * IMPORTANT: Each process() needs to be followed by a logEnvironmentReturn()
	 * @param senses - the sensory system
	 * @return action decision
	 */
	public int process(HumanSensory senses) {

		/** TESTING----------------------------------------------------------------------------------------------------=
		System.out.println("memory:\n" + memory);
		/** TESTING----------------------------------------------------------------------------------------------------=
		 */

		// process state and update memory
		memory.push(MRU.feed(memory.peek(), senses.produceState(takenAction, numPercept)));

		double[] MRUOutput = memory.peek().h();
		// get actor's action opinion on output of MRU
	    probs = ACU.feed(MRUOutput, true);
		// get critic's state-value opinion
		V_s = ACU.feed(MRUOutput, false)[0];

		/** TESTING----------------------------------------------------------------------------------------------------=
		System.out.print("PROBS: ");
		for (int i = 0; i < numAction; ++i) System.out.print(probs[i] + ", ");
		System.out.println("V_s: " + V_s);
		/** TESTING----------------------------------------------------------------------------------------------------=
		 */

		if (explorationRate > Math.random() && !inTesting) { // don't explore during testing
			explorationRate *= ER_RR; // slowly going towards exploitation
			takenAction = actionTable[(int) (Math.random() * numAction)];
		} else {
			// stochastic action selection
			takenAction = -1; // impossible value
			double num = Math.random(), p = 0;
			for (int i = 0; i < numAction; ++i) { // only first half is result of softmax
				p += probs[i];
				if (num < p) {
					takenAction = actionTable[i];
					memory.peek().set_a(i);
					break;
				}
			}
			if (takenAction == -1) throw new IllegalStateException("Stochastic action selection fails.");
		}

		/* // protocol uses maximum probability
			double max = probs[0];
			int argmax = 0;
			for (int i = 1; i < numAction; ++i) {
				if (probs[i] > max) {
					max = probs[i];
					argmax = i;
				}
			}
			takenAction = actionTable[argmax];
		}
		 */

		/** TESTING----------------------------------------------------------------------------------------------------=
		System.out.println("**Choose action " + takenAction);
         /** TESTING----------------------------------------------------------------------------------------------------=
         */

		return takenAction;
	    
	}

	/**
	 * Logs the environment return after an action is taken
	 * @param reward - the returned reward
	 * @param senses - the sensory system
	 */
	public void logEnvironmentReturn(int reward, HumanSensory senses) {

		// bootstrap oracle state-value using TD(0) target
		double V_snext = ACU.feed(MRU.feed(memory.peek(), senses.produceState(takenAction, numPercept)).h(), false)[0];
		TDTarget = reward + GAMMA * V_snext;

		// update latest memory unit
		memory.peek().set_r(reward);
		memory.peek().set_cG(TDTarget - V_s);
		memory.peek().set_aG((TDTarget - V_s) / probs[memory.peek().a()]);

		/** TESTING----------------------------------------------------------------------------------------------------=
		 System.out.println("**Logged " + memory.peek());
		 /** TESTING----------------------------------------------------------------------------------------------------=
		 */

	}

	/**
	 * Tunes the ACU and MRU at the end of a life
	 */
	public void adjust() {

		if (inTesting) throw new IllegalStateException("Adjust should not happen during testing.");

		// for collecting updates to weights in ACU
		HashMap<Integer, Double> actorConGrads = new HashMap<>();
		HashMap<Integer, Double> criticConGrads = new HashMap<>();

		// for collecting updates to weights in MRU
		HashMap<Integer, Double> fConGrads = new HashMap<>();
		HashMap<Integer, Double> iConGrads = new HashMap<>();
		HashMap<Integer, Double> cConGrads = new HashMap<>();
		HashMap<Integer, Double> oConGrads = new HashMap<>();

		double[] dL_dCnext = new double[MRU.getOutputNum()];
		double[] dL_dhnext = new double[MRU.getOutputNum()];

		while (memory.peek().t() >= 0) { // done when see a negative time step

			MemUnit memUnit = memory.pop();
			// for collecting gradient returns of ACU
			HashMap<Integer, Double> actorGradRet = new HashMap<>();
			HashMap<Integer, Double> criticGradRet = new HashMap<>();
			// train critic using MSE gradient and actor using critic's opinion and Advantage Function
			ACU.trainActorCritic(memUnit, actorConGrads, criticConGrads, actorGradRet, criticGradRet);
			// train MRU gates using actor's and critic's back gradients
			MRU.trainGates(memUnit, memory.peek(), dL_dCnext, dL_dhnext,
					actorGradRet, criticGradRet, fConGrads, iConGrads, cConGrads, oConGrads);

		}

		// update all weights of actor and critic
		for (ACUConGene con : ACU.getCons().getData()) {
			int IN = con.getIN();
			if (con.isEnabled()) {
				con.setWeight(con.getWeight(true) + (actorConGrads.containsKey(IN) ? actorConGrads.get(IN) : 0), true);
				con.setWeight(con.getWeight(false) + (criticConGrads.containsKey(IN) ? criticConGrads.get(IN) : 0), false);
			}
		}

		// update all weights of MRU gates
		for (MRUConGene con : MRU.getCons().getData()) {
			int IN = con.getIN();
			if (con.isEnabled()) {
				con.setWeight(con.getWeight('o') + (oConGrads.containsKey(IN) ? oConGrads.get(IN) : 0), 'o');
				con.setWeight(con.getWeight('c') + (cConGrads.containsKey(IN) ? cConGrads.get(IN) : 0), 'c');
				con.setWeight(con.getWeight('i') + (iConGrads.containsKey(IN) ? iConGrads.get(IN) : 0), 'i');
				con.setWeight(con.getWeight('f') + (fConGrads.containsKey(IN) ? fConGrads.get(IN) : 0), 'f');
			}
		}

	}
	
}