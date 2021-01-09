class Environment {
	
	// geography map of the cave
	private char[][][] caveGeography;
	private int caveSize;
	
	// perceivable effect map of the cave for the agent, should not be visible to wumpus
	private char[][][] perceptMap;
	// perceivable effect map of the cave for the wumpus, should not be visible to agent
	private char[][][] wumpusPerceptMap;
	
	// pass a copy of the agent and wumpus agent in for monitoring
	private Agent agent;
	private WumpusAgent wagent;
	// monitoring variables
	private int[] agentPosition;
	private int[] wumpusPosition;
	// force fields wumpus-size and agent-size
	private boolean forceField;
	private boolean w_forceField;
	private boolean scream;
	
	// visualization bars
	private String bar;
	
	public Environment(int size, char[][][] worldBlueprint) {
	
		caveSize = size;
		
		// 3rd array represents:
		// 0 - Pit room - 'P' for has pit and ' ' for no pit
		// 1 - Wumpus presence - can be 'W', 'M', '{', '}' and ' ' for no wumpus
		// 2 - Gold - 'G' for has gold and ' ' for no gold
		// 3 - Agent presence - can be 'A', '>', 'V', '<' and ' ' for no agent
		caveGeography = new char[caveSize][caveSize][4];
		
		// 3rd array represents:
		// 0 - Breeze effect - 'B' for has breeze and ' ' for no breeze
		// 1 - Stench effect - 'S' for has stench and ' ' for no stench
		// 2 - Glitter effect - 'G' for has glitter and ' ' for no glitter
		// 3 - Humany effect - 'H' for has humany and ' ' for no humany
		perceptMap = new char[caveSize][caveSize][4];
		
		// 3rd array represents:
		// 0 - Scent lifetime effect - '0' for end of life time
		// 1 - Scent direction - can be 'A', '>', 'V', '<', and 'X' for current position
		// 2 - Breeze effect - 'B' for has breeze and ' ' for no breeze
		// 3 - Humany effect - 'H' for has humany and ' ' for no humany
		wumpusPerceptMap = new char[caveSize][caveSize][4];
		
		agentPosition = new int[2];
		wumpusPosition = new int[2];
		
		// no force field is raised and no scream yet
		forceField = false;
		w_forceField = false;
		scream = false;
		
		// store world definition
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
				for (int k = 0; k < 4; k++) {
					caveGeography[i][j][k] = worldBlueprint[i][j][k];	
				}
			}
		}
		
		// initialize percept map
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
				for (int k = 0; k < 4; k++) {
					perceptMap[i][j][k] = ' ';
				}
			}
		}
		
		// initialize wumpus percept map
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
				wumpusPerceptMap[i][j][0] = '0';
				wumpusPerceptMap[i][j][1] = ' ';
				wumpusPerceptMap[i][j][2] = ' ';
				wumpusPerceptMap[i][j][3] = ' ';
			}
		}
		
		// generate the perceivable effects such as breeze, stench, etc.
		generatePerceivableEffects();
		
		// generate the perceivable effects such as breeze, stench, etc.
		w_generatePerceivableEffects();
		
		// initialize bar to the empty string
		bar = "";
		
		// create divider bar for display output
		for (int i = 0; i < (caveSize * 5) + caveSize - 1; i++) {
			bar = bar + "-";
		}
				
	}
	
	public int getCaveSize() {
		return caveSize;
	}
	
	public char getAgentDirection() {
		
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
				switch (caveGeography[i][j][3]) {
				case 'A': return 'N';
				case '>': return 'E';
				case 'V': return 'S';
				case '<': return 'W';
				}
			}
		}
		return '@';
	}
	
	public char getWumpusDirection() {
		
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
				switch (caveGeography[i][j][1]) {
				case 'M': return 'N';
				case '}': return 'E';
				case 'W': return 'S';
				case '{': return 'W';
				}
			}
		}
		return '@';
	}
	
	public int[] getAgentLocation() {
		
		int[] agentPos = new int[2];
		
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
				if (agentIsThere (i, j)) {
					agentPos[0] = i;
					agentPos[1] = j;
				}
			}
		}
		return agentPos;	
		
	}
	
	public int[] getWumpusLocation() {
		
		int[] wumpusPos = new int[2];
		
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
				if (wumpusIsThere (i, j)) {
					wumpusPos[0] = i;
					wumpusPos[1] = j;
				}
			}
		}
		return wumpusPos;
		
	}
	
	public void setFF (boolean FF) {
		forceField = FF;
	}
	
	public boolean thereIsFF() {
		return forceField;
	}
	
	public void setScream(boolean screamed) {
		scream = screamed;
	}
	
	public boolean thereIsScream() {
		return scream;
	}
	
	// for agent to perceive breeze, stench, and glitter
	public boolean thereIsBreeze() {
		
		if (perceptMap[agent.myLocation()[0]][agent.myLocation()[1]][0] == 'B') 
			return true;
		else return false;
		
	}
	
	public boolean thereIsStench() {
		
		if (perceptMap[agent.myLocation()[0]][agent.myLocation()[1]][1] == 'S') 
			return true;
		else return false;
		
	}
	
	public boolean thereIsGlitter() {
		
		if (perceptMap[agent.myLocation()[0]][agent.myLocation()[1]][2] == 'G') 
			return true;
		else return false;
		
	}
	
	// for wumpus to perceive force field, breeze and agent's presence
	public void w_setFF (boolean WFF) {
		w_forceField = WFF;
	}
	
	public boolean w_thereIsBreeze() {
		
		if (wumpusPerceptMap[wagent.myLocation()[0]][wagent.myLocation()[1]][2] == 'B') 
			return true;
		else return false;
		
	}
	
	public boolean thereIsHuman() {

		if (wumpusPerceptMap[wagent.myLocation()[0]][wagent.myLocation()[1]][3] == 'H')
			return true;
		else return false;
		
	}
	
	public boolean w_thereIsFF() {
		return w_forceField;
	}

	public boolean grabGold() {
		
		// if gold is where agent is standing
		if (perceptMap[agent.myLocation()[0]][agent.myLocation()[1]][2] == 'G') {
			// delete the gold and glitter effect
			perceptMap[agent.myLocation()[0]][agent.myLocation()[1]][2] = ' ';
			caveGeography[agent.myLocation()[0]][agent.myLocation()[1]][2] = ' ';
			// gold has been taken by agent
			return true;
		}
		// reach here means there is no gold where agent is standing
		return false;
		
	}
	
	public boolean agentIsDead() {
		
		// if agent is in pit room, it is 100% dead
		if (caveGeography[agent.myLocation()[0]][agent.myLocation()[1]][0] == 'P') 
			return true;
		// if not, it is not dead
		return false;
		
	}
	
	public boolean wumpusSlamAgent() {
		
		// if agent is in the same room with wumpus and wumpus decides to slam, agent is 100% dead
		if (agent.myLocation()[0] == wagent.myLocation()[0] &&
			agent.myLocation()[1] == wagent.myLocation()[1])
			return true;
		// if not, it is not dead
		return false;
		
	}
	
	public boolean wumpusIsDead() {
		
		// if wumpus is in pit room, it is 100% dead
		if (caveGeography[wagent.myLocation()[0]][wagent.myLocation()[1]][0] == 'P') 
			return true;
		// if not, it is not dead
		return false;
		
	}
	
	// leave a fading humany scent with direction on the wumpus percept map
	private void lingerScent (int x, int y) {
		
		// give the scent a direction
		wumpusPerceptMap[x][y][1] = agent.myIcon();
		// remove the strong scent presence
		wumpusPerceptMap[x][y][3] = ' ';
		
	}
	
	// drop the agent onto the world
	public void descendAgent (Agent theAgent, boolean start) {
		
		// only when we are in the middle of a simulation
		if (!start) {
			// remove the agent presence at its current position
			caveGeography[agentPosition[0]][agentPosition[1]][3] = ' ';
			// make the humany scent linger around
			lingerScent (agentPosition[0], agentPosition[1]);
		}
		
		// save it for monitoring
		agent = theAgent;
		// visualization of the agent's direction
		caveGeography[agent.myLocation()[0]][agent.myLocation()[1]][3] = agent.myIcon();
		
		agentPosition[0] = agent.myLocation()[0];
		agentPosition[1] = agent.myLocation()[1];
		
	}
	
	// drop the wumpus agent onto the world
	public void descendWumpus (WumpusAgent theWumpus, boolean start) {
		
		// only when we are in the middle of a simulation
		if (!start) {
			// remove the wumpus agent presence at its current position
			removeWumpus (wumpusPosition[0], wumpusPosition[1], false);
		}
		
		// save it for monitoring
		wagent = theWumpus;
		// visualization of the agent's direction
		caveGeography[wagent.myLocation()[0]][wagent.myLocation()[1]][1] = wagent.myIcon();
		
		wumpusPosition[0] = wagent.myLocation()[0];
		wumpusPosition[1] = wagent.myLocation()[1];
		
	}
	
	// remove the wumpus from the environment
	public void removeWumpus (int x, int y, boolean isDead) {
		
		// if wumpus is dead, represent the wumpus as dead
		if (isDead)
			caveGeography[x][y][1] = '*';
		else
			caveGeography[x][y][1] = ' ';
		
		// if the room next to wumpus is out of cave bound
		// then there should be no stench there to clean
		perceptMap[x][y][1] = ' ';
		if (x-1 >= 0) perceptMap[x-1][y][1] = ' ';
		if (x+1 < caveSize) perceptMap[x+1][y][1] = ' ';
		if (y-1 >= 0) perceptMap[x][y-1][1] = ' ';
		if (y+1 < caveSize) perceptMap[x][y+1][1] = ' ';
		
	}
	
	// check if the agent is in the space
	private boolean agentIsThere (int x, int y) {
		
		char space = caveGeography[x][y][3];
		if (space == 'A' || space == 'V' || space == '<' || space == '>')
			return true;
		return false;
		
	}
	
	// check if the wumpus is in the space
	private boolean wumpusIsThere (int x, int y) {
		
		char space = caveGeography[x][y][1];
		if (space == 'W' || space == 'M' || space == '{' || space == '}')
			return true;
		return false;
		
	}
	
	// analyze the flying direction of a shot arrow
	public boolean shootArrow() {
		
		switch (agent.myDirection()) {
		case 'N':
			
			// if agent is facing north, should the wumpus be in the room above
			// (only considering the vertical column above the agent)
			for (int i = agent.myLocation()[0]; i < caveSize; i++) {
				if (wumpusIsThere (i, agent.myLocation()[1])) {
					removeWumpus (i, agent.myLocation()[1], true);
					return true;
				}
			}
			
		case 'E':
			
			// if agent is facing east, should the wumpus be in the room to the right
			// (only considering the horizontal column to the left of where the agent is)
			for (int i = agent.myLocation()[1]; i < caveSize; i++) {
				if (wumpusIsThere (agent.myLocation()[0], i)) {
					removeWumpus (agent.myLocation()[0], i, true);
					return true;
				}
			}
			
		case 'S':
			
			// if agent is facing north, should the wumpus be in the room below
			// (only considering the vertical column below the agent)
			for (int i = agent.myLocation()[0]; i >= 0; i--) {
				if (wumpusIsThere (i, agent.myLocation()[1])) {
					removeWumpus (i, agent.myLocation()[1], true);
					return true;
				}
			}
			
		case 'W':
			
			// if agent is facing east, should the wumpus be in the room to the left
			// (only considering the horizontal column to the right of where the agent is)
			for (int i = agent.myLocation()[1]; i >= 0; i--) {
				if (wumpusIsThere (agent.myLocation()[0], i)) {
					removeWumpus (agent.myLocation()[0], i, true);
					return true;
				}
			}
			
		}
		// reach here means the arrow is not going to hit the wumpus
		return false;
		
	}
	
	// regulate the life time of a humany scent until it fades completely
	public void regulateScentLifetime() {
		
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
				// clean up the direction track if scent is out of lifetime
				if (wumpusPerceptMap[i][j][0] == '0')
					wumpusPerceptMap[i][j][1] = ' ';
				// otherwise, decrease scent life time
				else
					wumpusPerceptMap[i][j][0] -= 1;
				
			}
		}	
		
	}
	
	// generate all the effects perceivable to the agent in the environment
	public void generatePerceivableEffects() {
		
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
					
				// blow the breeze around where the pit is
				if (caveGeography[i][j][0] == 'P') {
					perceptMap[i][j][0] = 'B';
					if (j-1 >= 0) perceptMap[i][j-1][0] = 'B';
					if (i+1 < caveSize) perceptMap[i+1][j][0] = 'B';
					if (j+1 < caveSize) perceptMap[i][j+1][0] = 'B';
					if (i-1 >= 0) perceptMap[i-1][j][0] = 'B';
				}
				// emit the stench around and at where the wumpus is
				else if (wumpusIsThere (i, j)) {
					perceptMap[i][j][1] = 'S';
					if (j-1 >= 0) perceptMap[i][j-1][1] = 'S';
					if (i+1 < caveSize) perceptMap[i+1][j][1] = 'S';
					if (j+1 < caveSize) perceptMap[i][j+1][1] = 'S';
					if (i-1 >= 0) perceptMap[i-1][j][1] = 'S';
				}
				// shine glitter where the gold is
				else if (caveGeography[i][j][2] == 'G') 
					perceptMap[i][j][2] = 'G';
					
			}
		}	
	}
	
	// generate all the effects to the wumpus in the environment
	public void w_generatePerceivableEffects() {
		
		for (int i = 0; i < caveSize; i++) {
			for (int j = 0; j < caveSize; j++) {
					
				// emit the humany scent at where the agent is
				if (agentIsThere (i, j)) {
					// give the scent a life time
					// the value can only be changed from here!
					wumpusPerceptMap[i][j][0] = '3';
					// scent don't need to have a direction
					wumpusPerceptMap[i][j][1] = 'X';
					// only need the strong humany scent
					wumpusPerceptMap[i][j][3] = 'H';
				}
				// blow the breeze around where the pit is
				else if (caveGeography[i][j][0] == 'P') {
					wumpusPerceptMap[i][j][2] = 'B';
					if (j-1 >= 0) wumpusPerceptMap[i][j-1][2] = 'B';
					if (i+1 < caveSize) wumpusPerceptMap[i+1][j][2] = 'B';
					if (j+1 < caveSize) wumpusPerceptMap[i][j+1][2] = 'B';
					if (i-1 >= 0) wumpusPerceptMap[i-1][j][2] = 'B';
				}
					
			}
		}	
	}
	
	public void visualizeEnvironment() {
			
		// this printing is not interactive so it will need to be change based on the world size
		System.out.println("\nGeography                       . Perceivable to agent            . Perceivable to wumpus");
		// interactive printing
		System.out.println(" " + bar + "  .  " + bar + "  .  " + bar);
		// visualize the cave
		for (int i = caveSize-1; i > -1; i--) {
			for (int j = 0; j < 2; j++) {
				// visualize each room with the cave geography map
				for (int k = 0; k < caveSize; k++) {
					
					if (j == 0)
						System.out.print("| " + caveGeography[i][k][0] + " " + caveGeography[i][k][1] + " ");
					else
						System.out.print("| " + caveGeography[i][k][2] + " " + caveGeography[i][k][3] + " ");
					
					if (k == caveSize-1) {
						System.out.print("| .");
						
					}
				}
				System.out.print(" ");
				// visualize each room in the percept map
				for (int k = 0; k < caveSize; k++) {
					
					if (j == 0) {
						System.out.print("| " + perceptMap[i][k][0] + " " + perceptMap[i][k][1] + " ");	
					}
					else {
						System.out.print("| " + perceptMap[i][k][2] + " " + perceptMap[i][k][3] + " ");
					}
					
					if (k == caveSize-1) {
						System.out.print("| .");
					}
					
				}
				System.out.print(" ");
				// visualize each room in the wumpus percept map
				for (int k = 0; k < caveSize; k++) {
					
					if (j == 0) {
						System.out.print("| " + handle (wumpusPerceptMap[i][k][0]) + " " + wumpusPerceptMap[i][k][1] + " ");	
					}
					else {
						System.out.print("| " + wumpusPerceptMap[i][k][2] + " " + wumpusPerceptMap[i][k][3] + " ");
					}
					
					if (k == caveSize-1) {
						System.out.print("|");
					}
					
				}
				System.out.print("\n");
			}
			// separate a row of room with the next one
			System.out.println(" " + bar + "  .  " + bar + "  .  " + bar);
		}
		System.out.print("\n");
	}
	
	// make the 0s disappear for better viewing
	private char handle (char ch) {
		if (ch == '0') return ' ';
		return ch;
	}
	
}