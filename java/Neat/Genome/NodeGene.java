package Neat.Genome;

public class NodeGene extends Gene {

    private double x, y;

    public NodeGene(int n) {
        super(n);
    }

    public boolean equals(Object o) {
        if (!(o instanceof NodeGene))
            return false;
        return IN == ((NodeGene) o).getIN();
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setX(double val) {
        x = val;
    }
    public void setY(double val) {
        y = val;
    }

    public int hashCode() {
        return IN;
    }

}
