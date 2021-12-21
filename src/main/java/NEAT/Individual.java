package NEAT;

import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.MemoryHead.MHg;

public class Individual implements Cloneable {

    private double score;
    private Species species;
    private Species parentSpecies;
    private final MHg memoryRetentionUnit;
    private final DHg actorCriticUnit;

    public Individual(MHg mru, DHg acu) {
        parentSpecies = null; // most ancient predecessor doesn't have a parent
        score = 0;
        species = null;
        memoryRetentionUnit = mru;
        actorCriticUnit = acu;
    }

    public void setParentSpecies(Species s) { parentSpecies = s; }
    public Species getParentSpecies() { return parentSpecies; }

    public void express() {
        memoryRetentionUnit.express();
        actorCriticUnit.express();
    }

    public void replaceWith(Individual newborn) { // keep the score of the dead ones
        score = Double.NEGATIVE_INFINITY; // signify a new born so don't use it as fittest selection
        species = null; // in case asexual reproduction (clone) assign a species
        // species is null when replace is invoked
        // parent species is assigned right after replace complete
        memoryRetentionUnit.cast(newborn.memoryRetentionUnit);
        actorCriticUnit.cast(newborn.actorCriticUnit);
    }

    public double getScore() { return score; }
    public void setScore(double s) { score = s; }

    public Species getSpecies() { return species; }
    public void setSpecies(Species s) { species = s; }

    public MHg getMRU() { return memoryRetentionUnit; }
    public DHg getACU() { return actorCriticUnit; }

    public boolean equals(Object o) {
        if (!(o instanceof Individual))
            return false;
        return (memoryRetentionUnit.getID() == ((Individual) o).memoryRetentionUnit.getID()) &&
                (actorCriticUnit.getID() == ((Individual) o).actorCriticUnit.getID());
    }

    @Override
    public String toString() {
        return "<Individual/score=" + (Math.round(score * 100.0) / 100.0) + "\n" + memoryRetentionUnit + "\n" + actorCriticUnit + "/>";
    }

    @Override
    public Individual clone() throws CloneNotSupportedException {

        super.clone();
        Individual theClone = new Individual(memoryRetentionUnit.clone(), actorCriticUnit.clone());

        theClone.score = score; // in case asexual reproduction, this will become -inf anyway
        theClone.species = species; // in case asexual reproduction, this will be null-out anyway
        theClone.parentSpecies = parentSpecies; // in case asexual reproduction, this will get re-assign anyway

        return theClone;

    }

}
