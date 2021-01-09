// this class brings all intelligence components together and tests run them
// from the moment the agents are created in the world until it is terminated
class Simulation {
	
	// reward attributes for agent___________________________________________________________________________
	// they can be changed only from here!
	private static int goldReward = 1000;
	private static int actionCost = -1;
	private static int deathCost = -1000;
	private static int shootCost = -10;
	private static int getZappedCost = -1;
	
	int currScore = 0;
	int stepCounter = 1;
	// 0 represents agent waking up
	int takenAction = 0;
	
	// reward attributes for wumpus__________________________________________________________________________
	// they can be changed only from here!
	static int killReward = 1000;
	
	int wcurrScore = 0;
	int wstepCounter = 1;
	// 0 represents wumpus waking up
	int wtakenAction = 0;
		
	// intelligence attributes ______________________________________________________________________________
	Agent agent;
	InnerPerceivedData pd;
	
	WumpusAgent wumpus;
	WumpusInnerPerceivedData wpd;
	
	Environment environment;
	
	// simulation switch ____________________________________________________________________________________
	private boolean simulationRunning;
	
	public Simulation(Environment E, int maxSteps) {
		
		// start the simulator
		simulationRunning = true;
		// save the environment
		environment = E;
		// initialize the perceive-to-data functions database based on the environment
		pd = new InnerPerceivedData(environment);
		wpd = new WumpusInnerPerceivedData(environment);
		// put that database into the agent and wumpus
		agent = new Agent(environment, pd);
		wumpus = new WumpusAgent(environment, wpd);
		// drop the agent and wumpus into the environment
		environment.descendAgent (agent, true);
		environment.descendWumpus (wumpus, true);

	    // wake the agent and wumpus up
 		System.out.println("\nBoth waking up...");
	    // print the created environment out
	    environment.visualizeEnvironment();
	    
		// print out the current percept sequences that the agent and wumpus are experiencing
	    System.out.print("Agent << Percept: ");
		pd.printCurrentPerceptSequence();
		System.out.print("Wumpus << Percept: ");
		if (!wumpus.IAmDead())
			wpd.printCurrentPerceptSequence();
		else
			System.out.println("??");
		System.out.println("***");
		
		// always start with the score of 0
		System.out.println("Agent << Current score: " + currScore);
		System.out.println("Wumpus << Current score: " + wcurrScore);
		System.out.println("***");

		// keep running the simulation
		while (true) {
						
			// do not exceed the allowed max number of steps and make sure
			// the simulation switch is on
			if (stepCounter == maxSteps) {
				wumpus.selfTerminate();
				System.out.println("\nWumpus die of old age!");
			}
			
			if (stepCounter == maxSteps || simulationRunning == false) {
				// agent and wumpus terminate themselves
				agent.selfTerminate();
				wumpus.selfTerminate();
				
				// pre-final messages
				System.out.println("Agent << Last taken action: " + ActionSet.printAction (takenAction));
				System.out.println("Agent << Reach time step no. " + stepCounter);
				System.out.println("***");
				System.out.println("Wumpus << Last taken action: " + WumpusActionSet.printAction (wtakenAction));
				System.out.println("Wumpus << Reach time step no. " + wstepCounter);
				System.out.println("***");
				
				// dynamic messaging
				if (stepCounter == maxSteps && wstepCounter != maxSteps)
					System.out.println("\nAgent die of old age!\nSimulation stopped!");
				else if (stepCounter == maxSteps && wstepCounter == maxSteps)
					System.out.println("\nBoth die of old age! What's the odd of that!\nSimulation stopped!");
				else
					System.out.println("\nSimulation completed!");
				// save the actions for display later
				takenAction = ActionSet.SELF_TERMINATE;
				wtakenAction = WumpusActionSet.SELF_TERMINATE;
				
				// final visualization of this simulation before total world termination
				System.out.println("Final environment: ");
				environment.visualizeEnvironment();
				
				System.out.println("Agent << Final score: " + currScore);
				System.out.println("Agent << Final taken action: " + ActionSet.printAction (takenAction));
				System.out.println("***");
				System.out.println("Wumpus << Final score: " + wcurrScore);
				System.out.println("Wumpus << Final taken action: " + WumpusActionSet.printAction (wtakenAction));
				break;
			}
			
			// regulate the humany scents on the wumpus percept map
			environment.regulateScentLifetime();
			
			// print out the action that the agent just took
			//System.out.println("Agent << Taken action: " + ActionSet.printAction (takenAction));
			//System.out.println("Agent << Time step no. " + stepCounter);
			//System.out.println("***");
			//System.out.println("Wumpus << Taken action: " + WumpusActionSet.printAction (wtakenAction));
			//System.out.println("Wumpus << Time step no. " + wstepCounter);
			System.out.println("\n___________________________________________");
			
			// call to the agent's and wumpus's percept-action function and actuate it on the environment
			System.out.println("Agent act:");
			handleAction (agent.whatToDoNow());
			System.out.println("***");
			
			// wumpus will always act 1 step later than the agent
			System.out.println("\nWumpus act:");

			//handleWumpusAction (WumpusActionSet.NO_OP); //<---- for testing

			if (wumpus.IAmDead()) {
				handleWumpusAction (WumpusActionSet.NO_OP);
				environment.removeWumpus (wumpus.myLocation()[0], wumpus.myLocation()[1], true);
			}
			else {
				handleWumpusAction (wumpus.whatToDoNow());
				wstepCounter += 1;
			}


			// print out the changed environment
			environment.visualizeEnvironment();
			
			// print out the current percepts that the agent and wumpus are experiencing
			System.out.print("Agent << Percept: ");
			pd.printCurrentPerceptSequence();
			System.out.print("Wumpus << Percept: ");
			if (!wumpus.IAmDead())
				wpd.printCurrentPerceptSequence();
			else
				System.out.println("??");
			System.out.println("***");
							
			System.out.println("Agent << Current score: " + currScore);
			System.out.println("Wumpus << Current score: " + wcurrScore);
			System.out.println("***");
			
			// if the agent got the gold or is dead, end the simulation
			if (agent.IHaveGold()) {
				System.out.println("\nAgent found the GOLD!!");
				break;
			}
			
			// none of ending condition is met so go to next step
			stepCounter += 1;
			
		}
		
	}
	
	// get the agent ending score of this simulation
	public int getScore() {
		return currScore;
	}
	
	// get the wumpus ending score of this simulation
	public int getWumpusScore() {
		return wcurrScore;
	}
	
	// agent dynamics =======================================================================================
	// only for handleAction() function
	private void updateEnvironmentAfterAgent() {
		
		// place the agent in the place where is should be at
		// after doing an action
		environment.descendAgent (agent, false);
		// update percept maps
		environment.generatePerceivableEffects();
		environment.w_generatePerceivableEffects();
		// turn off the force field if it was previously on
		if (environment.thereIsFF())
			environment.setFF(false);
		// the screaming is done if there was screaming
		if (environment.thereIsScream()) 
			environment.setScream(false);
		
	}
	
	// actuate the agent's action decision
	public void handleAction (int action) {
		
		// must be careful when moving forward ______________________________________________________________
		if (action == ActionSet.MOVE_FORWARD) {
			
			System.out.println("Moving forward...");
			// save the action for display later
			takenAction = ActionSet.MOVE_FORWARD;
			// induce the cost
			currScore += actionCost;
			// let the agent move forward
			// but if it is about to fall out
			// of the world
			if (agent.moveForward() == false) {
				// generate force field and zap the agent
				// no need to update the agent's location
				environment.setFF (true);
				// place the agent in the place where is should be at
				// after doing an action
				environment.descendAgent (agent, false);
				// update percept maps
				environment.generatePerceivableEffects();
				environment.w_generatePerceivableEffects();
				// the screaming is done if there was screaming
				if (environment.thereIsScream()) 
					environment.setScream(false);
				// induce the cost of hitting force field
				currScore += getZappedCost;
				System.out.println("Zapped! Agent is repelled!");
				return;
			}
			updateEnvironmentAfterAgent();
			// if moving forward kills the agent
			if (environment.agentIsDead()) {
				// induce death cost and turn off the simulation
				currScore += deathCost;
				simulationRunning = false;
			}
			
		}
		// simply turn right ________________________________________________________________________________
		else if (action == ActionSet.TURN_RIGHT) {
			
			System.out.println("Turning right...");
			// save the action for display later
			takenAction = ActionSet.TURN_RIGHT;
			// induce the cost
			currScore += actionCost;
			// let the agent turn right
			agent.turnRight();
			updateEnvironmentAfterAgent();
			
		}
		// simply turn left _________________________________________________________________________________
		else if (action == ActionSet.TURN_LEFT) {
			
			System.out.println("Turning left...");
			// save the action for display later
			takenAction = ActionSet.TURN_LEFT;
			// induce the cost
			currScore += actionCost;
			// let the agent turn left
			agent.turnLeft();		
			updateEnvironmentAfterAgent();
			
		}
		// reach down and grab ______________________________________________________________________________
		else if (action == ActionSet.GRAB) {
			
			System.out.println("Reaching down...");
			// save the action for display later
			takenAction = ActionSet.GRAB;
			// induce the cost
			currScore += actionCost;
			// if the agent can grab gold at the room
			if (environment.grabGold() == true) {
				// let the agent take the gold
				agent.takeGold();
				// give the agent reward and turn of the simulation
				currScore += goldReward;
				simulationRunning = false;
			}
			else
				System.out.println("No gold here! Futile action!");
			updateEnvironmentAfterAgent();
			
		}
		// raise bow and shoot if possible __________________________________________________________________
		else if (action == ActionSet.SHOOT) {
			
			System.out.println("Calibrating...");
			// save the action for display later
			takenAction = ActionSet.SHOOT;
			// if shooting is still possible, induce the shooting cost
			if (agent.shoot() == true) {
				currScore += shootCost;	
				// if the arrow hit the wumpus, it will fill
				// the environment with scream and die
				if (environment.shootArrow()) {
					environment.setScream(true);
					wcurrScore += deathCost;
					wumpus.selfTerminate();
				}
				// place the agent in the place where is should be at
				// after doing an action
				environment.descendAgent (agent, false);
				// update percept maps
				environment.generatePerceivableEffects();
				environment.w_generatePerceivableEffects();
				// turn off the force field if it was previously on
				if (environment.thereIsFF())
					environment.setFF(false);
				return;
			}
			// otherwise, induce a different cost
			currScore += actionCost;
			updateEnvironmentAfterAgent();
			
		}
		// stand still ______________________________________________________________________________________
		else if (action == ActionSet.NO_OP) {
			
			System.out.println("...");
			// save the action for display later
			takenAction = ActionSet.NO_OP;
			updateEnvironmentAfterAgent();
			
		}
		
	}
	

	// wumpus dynamics ======================================================================================
	// only for handleWumpusAction() function
	private void updateEnvironmentAfterWumpus() {

		// place the wumpus in the place where is should be at
		// after doing an action
		environment.descendWumpus (wumpus, false);
		// update percept maps
		environment.generatePerceivableEffects();
		// turn off the force field if it was previously on
		if (environment.w_thereIsFF())
			environment.w_setFF(false);

		// if moving anywhere kills the agent
		if (environment.agentIsDead()) {
			// induce death cost on agent, reward wumpus, and turn off the simulation
			currScore += deathCost;
			wcurrScore += killReward;
			simulationRunning = false;
		}
		
	}
	
	// actuate the wumpus's action decision
	public void handleWumpusAction (int action) {
		
		// must be careful when moving forward ______________________________________________________________
		if (action == WumpusActionSet.MOVE_RIGHT) {
			
			System.out.println("Moving right...");
			// save the action for display later
			wtakenAction = WumpusActionSet.MOVE_RIGHT;
			// induce the cost
			wcurrScore += actionCost;
			// let the wumpus move forward
			// but if it is about to fall out
			// of the world
			if (wumpus.moveRight() == false) {
				// generate force field and zap the agent
				// no need to update the agent's location
				environment.w_setFF (true);
				// place the wumpus in the place where is should be at
				// after doing an action
				environment.descendWumpus (wumpus, false);
				// update percept maps
				environment.generatePerceivableEffects();
				// induce the cost of hitting force field
				wcurrScore += getZappedCost;
				System.out.println("Zapped! Wumpus is repelled!");
				return;
			}
			updateEnvironmentAfterWumpus();
			
		}
		if (action == WumpusActionSet.MOVE_LEFT) {

			System.out.println("Moving left...");
			// save the action for display later
			wtakenAction = WumpusActionSet.MOVE_LEFT;
			// induce the cost
			wcurrScore += actionCost;
			// let the wumpus move forward
			// but if it is about to fall out
			// of the world
			if (wumpus.moveLeft() == false) {
				// generate force field and zap the agent
				// no need to update the agent's location
				environment.w_setFF (true);
				// place the wumpus in the place where is should be at
				// after doing an action
				environment.descendWumpus (wumpus, false);
				// update percept maps
				environment.generatePerceivableEffects();
				// induce the cost of hitting force field
				wcurrScore += getZappedCost;
				System.out.println("Zapped! Wumpus is repelled!");
				return;
			}
			updateEnvironmentAfterWumpus();

		}
		if (action == WumpusActionSet.MOVE_UP) {

			System.out.println("Moving up...");
			// save the action for display later
			wtakenAction = WumpusActionSet.MOVE_UP;
			// induce the cost
			wcurrScore += actionCost;
			// let the wumpus move forward
			// but if it is about to fall out
			// of the world
			if (wumpus.moveUp() == false) {
				// generate force field and zap the agent
				// no need to update the agent's location
				environment.w_setFF (true);
				// place the wumpus in the place where is should be at
				// after doing an action
				environment.descendWumpus (wumpus, false);
				// update percept maps
				environment.generatePerceivableEffects();
				// induce the cost of hitting force field
				wcurrScore += getZappedCost;
				System.out.println("Zapped! Wumpus is repelled!");
				return;
			}
			updateEnvironmentAfterWumpus();

		}
		if (action == WumpusActionSet.MOVE_DOWN) {

			System.out.println("Moving down...");
			// save the action for display later
			wtakenAction = WumpusActionSet.MOVE_DOWN;
			// induce the cost
			wcurrScore += actionCost;
			// let the wumpus move forward
			// but if it is about to fall out
			// of the world
			if (wumpus.moveDown() == false) {
				// generate force field and zap the agent
				// no need to update the agent's location
				environment.w_setFF (true);
				// place the wumpus in the place where is should be at
				// after doing an action
				environment.descendWumpus (wumpus, false);
				// update percept maps
				environment.generatePerceivableEffects();
				// induce the cost of hitting force field
				wcurrScore += getZappedCost;
				System.out.println("Zapped! Wumpus is repelled!");
				return;
			}
			updateEnvironmentAfterWumpus();

		}
		// stand still ______________________________________________________________________________________
		else if (action == WumpusActionSet.NO_OP) {
			
			System.out.println("...");
			// save the action for display later
			wtakenAction = WumpusActionSet.NO_OP;
			updateEnvironmentAfterWumpus();
			
		}
		
	}
	
}