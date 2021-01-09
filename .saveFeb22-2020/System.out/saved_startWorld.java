public void startWorld (int worldSize, int numTrials, int maxSteps) {

		boolean randomAgentLoc = true;
		
		// world preparation ________________________________________________________________________________
		// generate the blue print for the world and feed it to the environment constructor
		char[][][] bluePrint = generateAWorldBluePrint (worldSize, randomAgentLoc);
	    Environment environment = new Environment(worldSize, bluePrint);
	    
		// start visualization ______________________________________________________________________________
		System.out.println("Dimensions: " + worldSize + "x" + worldSize);
	    System.out.println("Maximum number of steps: " + maxSteps);
	    System.out.println("Number of trials: " + numTrials);
	    System.out.println("Random Agent Location: " + randomAgentLoc);
	    
	    // start simulations ________________________________________________________________________________
	    // store the score for each trial in an array
	    int trialScores[] = new int[numTrials];
	    int trialWumpusScores[] = new int[numTrials];
	    
	    // keep running simulations until the number of trial runs out
	    for (int currTrial = 0; currTrial < numTrials; currTrial++) {
	    		    	
	    	// run the simulation trial
	    	Simulation trial = new Simulation (environment, maxSteps);
	    	// get the ending score of the trial
	    	trialScores[currTrial] = trial.getScore();
	    	trialWumpusScores[currTrial] = trial.getWumpusScore();
	    	
	    	// visualization divider between each trial
	    	System.out.println("\n\n___________________________________________\n");
	    	
	    	// next trial preparation
	    	// build another random world
	    	bluePrint = generateAWorldBluePrint (worldSize, randomAgentLoc);
		    environment = new Environment(worldSize, bluePrint);
	
	    	System.runFinalization();
	    }
	    
	    // finalize simulations _____________________________________________________________________________
	    // print out all the trial's score for agent
	    int totalScore = 0;
	    for (int i = 0; i < numTrials; i++) {
	    	
	    	System.out.println("Agent << Trial " + (i+1) + " score: " + trialScores[i]);
	    	totalScore += trialScores[i];
	    	
	    }
	    // print out the total score, average score, and finish
	    System.out.println("\nAgent << Total Score: " + totalScore);
	    System.out.println("\nAgent << Average Score: " + ((double)totalScore/(double)numTrials));
		System.out.println("\n***");	
		
		totalScore = 0;
		// print out all the trial's score for wumpus
	    for (int i = 0; i < numTrials; i++) {
	    	
	    	System.out.println("Wumpus << Trial " + (i+1) + " score: " + trialWumpusScores[i]);
	    	totalScore += trialWumpusScores[i];
	    	
	    }
	    // print out the total score, average score, and finish
	    System.out.println("\nWumpus << Total Score: " + totalScore);
	    System.out.println("\nWumpus << Average Score: " + ((double)totalScore/(double)numTrials));
		System.out.println("\nFinished.");

	}