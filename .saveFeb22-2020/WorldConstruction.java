import java.util.Random;

// this class constructs of the world in which the agent will be
// simulated in and extract the evaluation of the agent's behavior upon
// the world and agent termination
class WorldConstruction {
	
	public static char[][][] generateAWorldBluePrint (int size, boolean randomlyPlaceAgent) {
		
		// make a world
		char[][][] newWorld = new char[size][size][4];
		// make a mirror world to communicate what space
		// has already been occupied
		boolean[][] occupied = new boolean[size][size];
		// specify the number of pits and gold
		// they can be changed only from here!
		int pits = 2;
		int golds = 1;
		// make the coordinates for everything
		int x, y;
		
		Random randGen = new Random();
		
		// initialize the world geography as nothing
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int k = 0; k < 4; k++) {
					newWorld[i][j][k] = ' '; 
				}
			}
		}
		
		// no space is occupied yet
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				occupied[i][j] = false;
			}
		}
	    
		// Agent Generation _________________________________________________________________________________
		// default agent location
		// and orientation
		int agentXLoc = 0;
		int agentYLoc = 0;
		char agentIcon = 'A';
		
		// randomly generate agent
		// location and orientation
		if (randomlyPlaceAgent == true) {
			
			agentXLoc = randGen.nextInt(size);
			agentYLoc = randGen.nextInt(size);
			
			switch (randGen.nextInt(4)) {
				
				case 0: agentIcon = 'A'; break;
				case 1: agentIcon = '>'; break;
				case 2: agentIcon = 'V'; break;
				case 3: agentIcon = '<'; break;
			}
			
		}
		
		// show that space is now occupied with the agent
		// this is unnecessary as we have already saved
		// the chosen agent's location
		occupied[agentXLoc][agentYLoc] = true;
		// place agent in the world
		newWorld[agentXLoc][agentYLoc][3] = agentIcon;

		// Wumpus Generation ________________________________________________________________________________
		// randomly generate wumpus
		// location and orientation
		x = randGen.nextInt(size);
		y = randGen.nextInt(size);
	     
		// keep generating random coordinate until an unoccupied coordinate is found
		// and wumpus should not be generated on top of the agent
		while (x == agentXLoc && y == agentYLoc) {
			x = randGen.nextInt(size);
			y = randGen.nextInt(size);   
		}
		
		char wumpusIcon = 'W';
		switch (randGen.nextInt(4)) {
			
			case 0: wumpusIcon = 'M'; break;
			case 1: wumpusIcon = '}'; break;
			case 2: wumpusIcon = '{'; break;
			case 3: wumpusIcon = 'W'; break;
		}
			
		
		// show that space is now occupied with the wumpus
		// this is unnecessary as we have already saved
		// the chosen agent's location
		occupied[x][y] = true;
		// place wumpus in the world
		newWorld[x][y][1] = wumpusIcon;
		
		// Pit generation ___________________________________________________________________________________
		for (int i = 0; i < pits; i++) {
	     
			x = randGen.nextInt(size);
			y = randGen.nextInt(size);
		    
			// keep generating random coordinate until an unoccupied coordinate is found
			// and pits should not be generated on top of the agent
			while ((x == agentXLoc && y == agentYLoc) | occupied[x][y] == true) {
				x = randGen.nextInt(size);
				y = randGen.nextInt(size);    	   
			}
		    
			// that space is now occupied with a pit
			occupied[x][y] = true;
			// represent that pit
			newWorld[x][y][0] = 'P';
		     
		}
		
		// Gold Generation __________________________________________________________________________________
		for (int i = 0; i < golds; i++) {
		     
			x = randGen.nextInt(size);
			y = randGen.nextInt(size);
		    
			// keep generating random coordinate until an unoccupied coordinate is found
			// and it is preferred that golds are not generated on top of the agent
			while ((x == agentXLoc && y == agentYLoc) | occupied[x][y] == true) {
				x = randGen.nextInt(size);
				y = randGen.nextInt(size);    	   
			}
		    
			// that space is now occupied with gold
			occupied[x][y] = true;
			// represent the gold
			newWorld[x][y][2] = 'G';
		     
		}
		return newWorld;
		
	}

	// ================================================ MAIN ================================================
	public static void main (String args[]) {
			
		// default values of our simulation _________________________________________________________________
		// they can be changed only from here!
		int worldSize = 5;
		int numTrials = 1;
		int maxSteps = 50;
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
	// ============================================ MAIN  ENDING ============================================
	
}