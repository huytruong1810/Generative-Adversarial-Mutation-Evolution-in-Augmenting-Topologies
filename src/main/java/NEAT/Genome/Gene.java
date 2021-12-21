package NEAT.Genome;

public abstract class Gene implements Cloneable {

    protected int IN; // historical marking/innovation number

    public Gene() {}

    public int getIN() { return IN; }
    public void setIN(int n) { IN = n; }

    public abstract String inspect();

    @Override
    public Gene clone() throws CloneNotSupportedException { throw new CloneNotSupportedException(); }

}
