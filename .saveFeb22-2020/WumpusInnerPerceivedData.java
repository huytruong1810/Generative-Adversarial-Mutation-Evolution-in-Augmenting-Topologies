// this class is the storage of perceive-to-data functions using
// the thing that the wumpus perceives from the environment and 
// output the data for the function that decides what action to take, 
// that is, the wumpus's agent function
class WumpusInnerPerceivedData {
	
	private Environment environment;
		
	public WumpusInnerPerceivedData(Environment E) {
		
		environment = E;
		
	}
	
	public boolean IFeelFF() {
		return environment.w_thereIsFF();
	}

	public boolean IFeelBreeze() {
		return environment.w_thereIsBreeze();
	}
	
	public boolean ISmellHuman() {
		return environment.thereIsHuman();
	}
	
	public void printCurrentPerceptSequence() {
		
		if (IFeelFF())
			System.out.print("<bump,");
		else
			System.out.print("<none,");
		
		if (IFeelBreeze())
			System.out.print("breeze,");
		else
			System.out.print("none,");
		
		if (ISmellHuman())
			System.out.print("humany>\n");
		else 
			System.out.print("none>\n");
		
	}
	
}