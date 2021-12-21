package NEAT.Genome;

public abstract class Genome implements Cloneable {

    private static int GlobalID = 0;
    protected int ID; // for DataBase
    protected int inputNum, outputNum;

    public Genome(int inN, int outN) {
        ID = GlobalID++;
        inputNum = inN;
        outputNum = outN;
    }

    public int getID() { return ID; }
    public int getInputNum() { return inputNum; }
    public int getOutputNum() { return outputNum; }

    public void cast(Genome g) {
        ID = g.ID;
        inputNum = g.inputNum;
        outputNum = g.outputNum;
    }
    public abstract void express();

    public boolean equals(Object o) {
        if (!(o instanceof Genome))
            return false;
        return ID == ((Genome) o).ID; // even for asexually produced clones
    }

    @Override
    public Genome clone() throws CloneNotSupportedException {
        super.clone();
        GlobalID--; // clone should not have a unique global ID
        return null;
    }

}
