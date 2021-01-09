class Agent {
	
	// agent's attributes ___________________________________________________________________________________
	
	// agent's location on the environment
	private int[] location;
	// agent's direction:
	private char direction;
	// agent only has 1 arrow
	private int numArrows = 1;
	
	// agent's status
	private boolean isDead, gotGold;
	// agent's knowledge about itself and the world
	private int worldSizeKnowledge;
	private InnerPerceivedData P;
	private AgentFunction agentFunc;
	
	// agent manipulations __________________________________________________________________________________
	
	// bring agent to life
	public Agent (Environment overWorld, InnerPerceivedData p) {
		
		// initial status of the agent is not dead and don't have gold
		this.isDead = false;
		this.gotGold = false;
		
		// let the agent receive knowledge of the world's size and how to perceive the world
		this.worldSizeKnowledge = overWorld.getCaveSize();
		this.P = p;
		
		// create a new set of agent function
		this.agentFunc = new AgentFunction(this.worldSizeKnowledge);
		
		// let the agent knows its initial location
		this.location = overWorld.getAgentLocation();
		this.direction = overWorld.getAgentDirection();
		
	}
		
	// agent self-termination
	public void selfTerminate() {
		this.isDead = true;
	}
	
	// get the agent's life status
	public boolean IAmDead() {
		return this.isDead;
	}
	
	// put gold in agent's pocket
	public void takeGold() {
		this.gotGold = true;
	}
	
	// get the agent's bank account status
	public boolean IHaveGold() {
		return this.gotGold;
	}
	
	// set the agent's location
	public void jumpTo (int[] here) {
		this.location[0] = here[0];
		this.location[1] = here[1];
	}
	
	// get the agent's location
	public int[] myLocation() {
		return this.location;
	}
	
	// get the agent's direction
	public char myDirection() {
		return this.direction;
	}
	
	// get the agent's icon based on its direction
	public char myIcon() {
		
		switch (this.direction) {
		case 'N': return 'A';
		case 'E': return '>';
		case 'S': return 'V';
		case 'W': return '<';
		}
		return '@';
		
	}
	
	// set the agent's direction
	public void setDirection(char newDirection) {
		this.direction = newDirection;
	}
	
	// set the agent's location
	public void setLocation(int[] newLocation) {
		this.location[0] = newLocation[0];
		this.location[1] = newLocation[1];
	}
	
	// return an appropriate action based on the percept
	// this is our percept-action mapping function
	public int whatToDoNow() {
		return this.agentFunc.process (this.P);
	}
	
	// move the agent 1 step forward
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
		// reach here means the agent will fall off the world
		return false;
		
	}
	
	// shoot an arrow if the agent still have arrows
	public boolean shoot() {
		
		if (this.numArrows == 1) {
			System.out.println ("Shooting! Hope it hits the Wumpus");
			this.numArrows -= 1;
			System.out.println ("I only has " + this.numArrows + " arrow(s) left!\n");
			return true;
		}
		else {
			System.out.println ("I am all out of arrow!\n");
			return false;
		}
		
	}
	
	// turn the agent to the left
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
	
	// turn the agent to the right
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