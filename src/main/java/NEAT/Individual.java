package NEAT;

import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.MemoryHead.MHg;

public class Individual implements Cloneable {

    private double score;
    private Species species;
    private Species parentSpecies;
    private final MHg MH;
    private final DHg DH;

    public Individual(MHg mru, DHg acu) {
        parentSpecies = null; // most ancient predecessor doesn't have a parent
        score = 0;
        species = null;
        MH = mru;
        DH = acu;
    }

    public void setParentSpecies(Species s) { parentSpecies = s; }
    public Species getParentSpecies() { return parentSpecies; }

    public void express() {
        MH.express();
        DH.express();
    }

    public void replaceWith(Individual newborn) { // keep the score of the dead ones
        score = Double.NEGATIVE_INFINITY; // signify a new born so don't use it as fittest selection
        species = null; // in case asexual reproduction (clone) assign a species
        // species is null when replace is invoked
        // parent species is assigned right after replace complete
        MH.cast(newborn.MH);
        DH.cast(newborn.DH);
    }

    public double getScore() { return score; }
    public void setScore(double s) { score = s; }

    public Species getSpecies() { return species; }
    public void setSpecies(Species s) { species = s; }

    public MHg getMH() { return MH; }
    public DHg getDH() { return DH; }

    public boolean equals(Object o) {
        if (!(o instanceof Individual))
            return false;
        return (MH.getID() == ((Individual) o).MH.getID()) &&
                (DH.getID() == ((Individual) o).DH.getID());
    }

    @Override
    public String toString() {
        return "<Individual/score=" + (Math.round(score * 100.0) / 100.0) + "\n" + MH + "\n" + DH + "/>";
    }

    @Override
    public Individual clone() throws CloneNotSupportedException {

        super.clone();
        Individual theClone = new Individual(MH.clone(), DH.clone());

        theClone.score = score; // in case asexual reproduction, this will become -inf anyway
        theClone.species = species; // in case asexual reproduction, this will be null-out anyway
        theClone.parentSpecies = parentSpecies; // in case asexual reproduction, this will get re-assign anyway

        return theClone;

    }

}
