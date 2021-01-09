// this class is the storage of perceive-to-data functions using
// the thing that the agent perceives from the environment and 
// output the data for the function that decides what action to take, 
// that is, the agent function
class InnerPerceivedData {
	
	private Environment environment;
		
	public InnerPerceivedData(Environment E) {
		
		environment = E;
		
	}
	
	public boolean IFeelFF() {
		return environment.thereIsFF();
	}
	
	public boolean ISeeGlitter() {
		return environment.thereIsGlitter();
	}

	public boolean IFeelBreeze() {
		return environment.thereIsBreeze();
	}

	public boolean ISmellStench() {
		return environment.thereIsStench();
	}
	
	public boolean IHearScream() {
		return environment.thereIsScream();
	}
	
	public void printCurrentPerceptSequence() {
		
		if (IFeelFF())
			System.out.print("<bump,");
		else
			System.out.print("<none,");
		
		if (ISeeGlitter())
			System.out.print("glitter,");
		else
			System.out.print("none,");
		
		if (IFeelBreeze())
			System.out.print("breeze,");
		else
			System.out.print("none,");
		
		if (ISmellStench())
			System.out.print("stench,");
		else
			System.out.print("none,");
			
		if (IHearScream())
			System.out.print("scream>\n");
		else
			System.out.print("none>\n");
		
	}
	
}