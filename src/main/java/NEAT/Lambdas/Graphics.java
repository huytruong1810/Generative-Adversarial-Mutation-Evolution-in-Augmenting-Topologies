package NEAT.Lambdas;

import NEAT.Genome.NodeGene;

import java.util.HashSet;
import java.util.function.BiFunction;

public class Graphics {

    public static HashSet<Double> OBS_NODES_Y = new HashSet<>();

    public static HashSet<Double> INF_NODES_Y = new HashSet<>();

    public static HashSet<Double> CRITIC_NODES_Y = new HashSet<>();

    public static HashSet<Double> SEER_NODES_Y = new HashSet<>();

    public static final BiFunction<Double, Double, Double> firstQuadY = (sy, ey) -> (3 * sy + ey) / 4.0 + (Math.random() * 200 - 100); // -100 to 100

    public static final BiFunction<Double, Double, Double> secondQuadY = (sy, ey) -> (sy + 3 * ey) / 4.0 + (Math.random() * 200 - 100); // -100 to 100

    public static final BiFunction<Integer, Integer, Double> initY = (i, s) -> (i + 1)/(double)(s + 1) * 1000;

    public static final BiFunction<NodeGene, NodeGene, Double> midY = (f, t) -> (f.getY() + t.getY()) / 2 + (Math.random() * 300 - 150); // -150 to 150

}
