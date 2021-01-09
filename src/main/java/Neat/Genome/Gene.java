package Neat.Genome;

public abstract class Gene {

    public int IN; // historical marking
    public Gene() {}
    public Gene(int n) { IN = n; }
    public int getIN() { return IN; }
    public void setIN(int n) { IN = n; }

}
