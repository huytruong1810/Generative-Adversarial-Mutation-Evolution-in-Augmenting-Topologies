package Wumpus;

import Environment.Environment;

public class WumpusSensory {
	
	private Environment E;
		
	public WumpusSensory(Environment e) {
		E = e;
	}

	public boolean IFeelBump() {
		return E.thereIsWWall();
	}
	public boolean ISmellPerson() { return E.thereIsScent(); }
	public char ISmellDirIs() { return E.getSceneDir(); }
	public char ISmellIntIs() { return E.getSceneInt(); }
	
}