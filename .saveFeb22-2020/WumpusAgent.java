class WumpusAgent {
	
	// wumpus agent's attributes ____________________________________________________________________________
	
	// wumpus agent's location on the environment
	private int[] location;
	// agent's direction:
	private char direction;
	
	// wumpus agent's status
	private boolean isDead, killAgent;
	// wumpus agent's knowledge about itself and the world
	private int worldSizeKnowledge;
	private WumpusInnerPerceivedData WP;
	private WumpusAgentFunction wumpusAgentFunc;
	
	// wumpus agent manipulations ___________________________________________________________________________
	
	// bring agent to life
	public WumpusAgent (Environment overWorld, WumpusInnerPerceivedData wp) {
		
		// initial status of the wumpus agent is not dead and haven't killed the agent
		this.isDead = false;
		this.killAgent = false;
		
		// let the wumpus agent receive knowledge of the world's size and how to perceive the world
		this.worldSizeKnowledge = overWorld.getCaveSize();
		this.WP = wp;
		
		// create a new set of agent function
		this.wumpusAgentFunc = new WumpusAgentFunction();
		
		// let the agent knows its initial location
		this.location = overWorld.getWumpusLocation();
		this.direction = overWorld.getWumpusDirection();
		
	}
		
	// wumpus agent self-termination
	public void selfTerminate() {
		this.isDead = true;
	}
	
	// get the wumpus agent's life status
	public boolean IAmDead() {
		return this.isDead;
	}
	
	// kill the agent
	public void killIt() {
		this.killAgent = true;
	}
	
	// get wumpus's murdering record
	public boolean IKilledIt() {
		return this.killAgent;
	}
	
	// set the wumpus agent's location
	public void jumpTo (int[] here) {
		this.location[0] = here[0];
		this.location[1] = here[1];
	}
	
	// get the wumpus agent's location
	public int[] myLocation() {
		return this.location;
	}
	
	// get the wumpus agent's direction
	public char myDirection() {
		return this.direction;
	}
	
	// get the agent's icon based on its direction
	public char myIcon() {
		
		switch (this.direction) {
		case 'N': return 'M';
		case 'E': return '}';
		case 'S': return 'W';
		case 'W': return '{';
		}
		return '@';
		
	}
	
	// set the wumpus agent's direction
	public void setDirection(char newDirection) {
		this.direction = newDirection;
	}
	
	// set the wumpus agent's location
	public void setLocation(int[] newLocation) {
		this.location[0] = newLocation[0];
		this.location[1] = newLocation[1];
	}
	
	// return an appropriate action based on the percept
	// this is our percept-action mapping function
	public int whatToDoNow() {
		return this.wumpusAgentFunc.process (this.WP);
	}
	
	// move the wumpus 1 step forward
	public boolean moveForward() {
		
		switch (this.direction) {
		case 'N':
			if (this.location[0] + 1 < this.worldSizeKnowledge) {
				this.location[0] += 1;
				return true;
			}
			break;
		case 'S':
			if (this.location[0] - 1 >= 0) {
				this.location[0] -= 1;
				return true;
			}
			break;
		case 'E':
			if (this.location[1] + 1 < this.worldSizeKnowledge) {
				this.location[1] += 1;
				return true;
			}
			break;
		case 'W':
			if (this.location[1] - 1 >= 0) {
				this.location[1] -= 1;
				return true;
			}
			break;
		}
		// reach here means the wumpus agent will fall off the world
		return false;
		
	}
	
	// turn the wumpus to the left
	public void turnLeft() {
		
		switch (this.direction) {
			case 'N':
				this.direction = 'W';
				break;
			case 'S':
				this.direction = 'E';
				break;
			case 'E':
				this.direction = 'N';
				break;
			case 'W':
				this.direction = 'S';
				break;
		}
		
	}
	
	// turn the wumpus to the right
	public void turnRight() {
		
		switch (this.direction) {
			case 'N':
				this.direction = 'E';
				break;
			case 'S':
				this.direction = 'W';
				break;
			case 'E':
				this.direction = 'S';
				break;
			case 'W':
				this.direction = 'N';
				break;
		}
		
	}
	
}