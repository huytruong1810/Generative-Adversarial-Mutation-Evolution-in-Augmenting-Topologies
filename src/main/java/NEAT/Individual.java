package NEAT;

import NEAT.Genome.ACU.ACUGenome;
import NEAT.Genome.MRU.MRUGenome;

public class Individual implements Cloneable {

    private double score;
    private Species species;
    private Species parentSpecies;
    private final MRUGenome memoryRetentionUnit;
    private final ACUGenome actorCriticUnit;

    public Individual(MRUGenome mru, ACUGenome acu) {
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

    public void replace(Individual other) { // keep the score of the dead ones
        score = Double.NEGATIVE_INFINITY; // signify a new born so don't use it as fittest selection
        memoryRetentionUnit.cast(other.memoryRetentionUnit);
        actorCriticUnit.cast(other.actorCriticUnit);
    }

    public double getScore() { return score; }
    public void setScore(double s) { score = s; }

    public Species getSpecies() { return species; }
    public void setSpecies(Species s) { species = s; }

    public MRUGenome getMRU() { return memoryRetentionUnit; }
    public ACUGenome getACU() { return actorCriticUnit; }

    public boolean equals(Object o) {
        if (!(o instanceof Individual))
            return false;
        return (memoryRetentionUnit.getID() == ((Individual) o).getMRU().getID()) && (actorCriticUnit.getID() == ((Individual) o).getACU().getID());
    }

    @Override
    public String toString() {
        return "<Individual/score=" + (Math.round(score * 100.0) / 100.0) + "\n" + memoryRetentionUnit + "\n" + actorCriticUnit + "/>";
    }

    @Override
    public Individual clone() throws CloneNotSupportedException {
        super.clone();
        Individual theClone = new Individual(memoryRetentionUnit.clone(), actorCriticUnit.clone());
        theClone.score = score;
        theClone.species = species;
        theClone.parentSpecies = parentSpecies;
        return theClone;
    }

}
