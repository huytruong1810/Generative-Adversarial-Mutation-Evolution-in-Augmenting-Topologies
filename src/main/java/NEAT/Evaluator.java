package NEAT;

import RL.Simulators.TestRoom;
import RL.Simulators.TrainRoom;

import java.util.concurrent.Callable;

import static NEAT.NEAT.numTeEps;
import static NEAT.NEAT.numTrEps;

public class Evaluator implements Callable<Double> {

    private final Individual evaluatee;
    private final TrainRoom trainer;
    private final TestRoom tester;
    private final char[][][] bluePrint;

    public Evaluator(Individual individual, TrainRoom trainRoom, TestRoom testRoom, char[][][] bp) {
        evaluatee = individual;
        trainer = trainRoom;
        tester = testRoom;
        bluePrint = bp;
    }

    @Override
    public Double call() {

        // training
        for (int e = 0; e < numTrEps; ++e) {
            trainer.reset(bluePrint, evaluatee, null);
            while (true) if (trainer.step() != 0) break;
        }

        // testing and report evaluation score
        double sumScore = 0.0;
        for (int e = 0; e < numTeEps; ++e) {
            tester.reset(bluePrint, evaluatee, null);
            while (true) if (tester.step() != 0) break;
            sumScore += tester.getHScore();                                                           // SPECIFICALLY FOR HUMAN ONLY RIGHT NOW
        }
        double avgScore = sumScore / numTeEps;
        evaluatee.setScore(avgScore);

        return avgScore;

    }

}
