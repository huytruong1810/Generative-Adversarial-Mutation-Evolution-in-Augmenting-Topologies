package Human;

import Environment.Environment;

public class HumanSensory {
	
	private Environment E;
		
	public HumanSensory(Environment e) { E = e; }
	
	public boolean IFeelBump() { return E.thereIsHWall(); }
	public boolean ISeeGlitter() { return E.thereIsGlitter(); }
	public boolean IFeelBreeze() { return E.thereIsBreeze(); }
	public boolean ISmellStench() { return E.thereIsStench(); }
	public boolean IHearScream() { return E.thereIsScream(); }
	
}