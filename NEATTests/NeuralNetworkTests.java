package NEATTests;

import NEAT.Genome.ACU.ACUConGene;
import NEAT.Genome.ACU.ACUGenome;
import NEAT.Genome.ACU.ACUNodeGene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Arrays;

import static NEAT.NEAT.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NeuralNetworkTests {

    ACUGenome g;
    ACUNodeGene in1, in2, in3, in4;
    ACUNodeGene out1, out2, out3;

    @BeforeEach
    public void setUpACU() {

        g = new ACUGenome(4, 3);

        in1 = new ACUNodeGene(1); in2 = new ACUNodeGene(2); in3 = new ACUNodeGene(3); in4 = new ACUNodeGene(4);
        out1 = new ACUNodeGene(5); out2 = new ACUNodeGene(6); out3 = new ACUNodeGene(7);

        in1.setX(hiddenNodeX); in2.setX(hiddenNodeX); in3.setX(hiddenNodeX); in4.setX(hiddenNodeX);
        out1.setX(outputNodeX); out2.setX(outputNodeX); out3.setX(outputNodeX);

        g.getNodes().addAll(in1, in2, in3, in4, out1, out2, out3);

    }

    private double r() { return Math.random() * 20 - 10; } // -10...10

    @RepeatedTest(10)
    public void testBasic() {

        double x1 = r(), x2 = r(), x3 = r(), x4 = r();
        double[] y_actor = softmax(new double[]{0, 0});
        double[] y_critic = new double[]{0};

        g.express();
        double[] y_actor_pred = g.feed(new double[]{x1, x2, x3, x4}, true);
        double[] y_critic_pred = g.feed(new double[]{x1, x2, x3, x4}, false);

        assertArrayEquals(y_actor, y_actor_pred);
        assertArrayEquals(y_critic, y_critic_pred);

    }

    private int stochasticSelection(double[] probs) {
        double num = Math.random(), c = 0;
        for (int i = 0; i < 2; ++i) {
            c += probs[i];
            if (num < c) return i;
        }
        return -1; // impossible
    }

    private double sigmoid(double x) { return 1 / (1 + Math.exp(-x)); }

    private double d_sigmoid(double s) { return s * (1 - s); }

    private double[] softmax(double[] x) {
        int n = x.length;
        double[] exp = new double[n], y = new double[n];
        for (int i = 0; i < n; ++i) exp[i] = Math.exp(x[i]);
        double sumExp = 0;
        for (double e : exp) sumExp += e;
        for (int i = 0; i < n; ++i) y[i] = exp[i] / sumExp;
        return y;
    }

    private void assertDoubleApprox(double expected, double actual) {
        double f = Math.pow(10, 12);
        assertEquals( Math.round(expected * f) / f, Math.round(actual * f) / f);
    }

    @RepeatedTest(100)
    public void testDense() {

        // VALUE SET UP
        double x1 = r(), x2 = r(), x3 = r(), x4 = r();
        double c_w11 = r(), c_w21 = r(), c_w31 = r(), c_w41 = r();
        double c_w12 = r(), c_w22 = r(), c_w32 = r(), c_w42 = r();
        double c_w13 = r(), c_w23 = r(), c_w33 = r(), c_w43 = r();
        double a_w11 = r(), a_w21 = r(), a_w31 = r(), a_w41 = r();
        double a_w12 = r(), a_w22 = r(), a_w32 = r(), a_w42 = r();
        double a_w13 = r(), a_w23 = r(), a_w33 = r(), a_w43 = r();

        // TRUE VALUE CALCULATION
        // FORWARDING
        double a_z1 = x1 * a_w11 + x2 * a_w21 + x3 * a_w31 + x4 * a_w41;
        double a_z2 = x1 * a_w12 + x2 * a_w22 + x3 * a_w32 + x4 * a_w42;

        double[] y_actor = softmax(new double[]{a_z1, a_z2});

        double c_z3 = x1 * c_w13 + x2 * c_w23 + x3 * c_w33 + x4 * c_w43;

        double[] y_critic = new double[]{c_z3};

        // BACK PROPAGATING
        int c = stochasticSelection(y_actor);
        double v = y_critic[0];
        double reward = r() * 10; // assume environment return

        double[] a_dL_dy = new double[2];
        a_dL_dy[c] = (reward - v) / y_actor[c];
        double a_dL_dy1 = a_dL_dy[0];
        double a_dL_dy2 = a_dL_dy[1];

        double exp_z1 = Math.exp(a_z1);
        double exp_z2 = Math.exp(a_z2);
        double S = exp_z1 + exp_z2;
        double a_dL_dz1 = a_dL_dy1 * (exp_z1 * (S - exp_z1))/Math.pow(S, 2) + a_dL_dy2 * (-exp_z2*exp_z1)/Math.pow(S, 2);
        double a_dL_dw11 = a_dL_dz1 * x1 * ACU_LR;
        double a_dL_dw21 = a_dL_dz1 * x2 * ACU_LR;
        double a_dL_dw31 = a_dL_dz1 * x3 * ACU_LR;
        double a_dL_dw41 = a_dL_dz1 * x4 * ACU_LR;
        double a_dL_dz2 = a_dL_dy2 * (exp_z2 * (S - exp_z2))/Math.pow(S, 2) + a_dL_dy1 * (-exp_z1*exp_z2)/Math.pow(S, 2);
        double a_dL_dw12 = a_dL_dz2 * x1 * ACU_LR;
        double a_dL_dw22 = a_dL_dz2 * x2 * ACU_LR;
        double a_dL_dw32 = a_dL_dz2 * x3 * ACU_LR;
        double a_dL_dw42 = a_dL_dz2 * x4 * ACU_LR;

        double c_dL_dy = reward - v;

        double c_dL_dz3 = c_dL_dy * 1;
        double c_dL_dw13 = c_dL_dz3 * x1 * ACU_LR;
        double c_dL_dw23 = c_dL_dz3 * x2 * ACU_LR;
        double c_dL_dw33 = c_dL_dz3 * x3 * ACU_LR;
        double c_dL_dw43 = c_dL_dz3 * x4 * ACU_LR;

        double a_dL_dx1 = a_dL_dz1 * a_w11 + a_dL_dz2 * a_w12;
        double a_dL_dx2 = a_dL_dz1 * a_w21 + a_dL_dz2 * a_w22;
        double a_dL_dx3 = a_dL_dz1 * a_w31 + a_dL_dz2 * a_w32;
        double a_dL_dx4 = a_dL_dz1 * a_w41 + a_dL_dz2 * a_w42;

        double c_dL_dx1 = c_dL_dz3 * c_w13;
        double c_dL_dx2 = c_dL_dz3 * c_w23;
        double c_dL_dx3 = c_dL_dz3 * c_w33;
        double c_dL_dx4 = c_dL_dz3 * c_w43;

        // GENOME SET UP
        ACUConGene c11 = new ACUConGene(in1, out1), c21 = new ACUConGene(in2, out1), c31 = new ACUConGene(in3, out1), c41 = new ACUConGene(in4, out1);
        ACUConGene c12 = new ACUConGene(in1, out2), c22 = new ACUConGene(in2, out2), c32 = new ACUConGene(in3, out2), c42 = new ACUConGene(in4, out2);
        ACUConGene c13 = new ACUConGene(in1, out3), c23 = new ACUConGene(in2, out3), c33 = new ACUConGene(in3, out3), c43 = new ACUConGene(in4, out3);
        c11.setWeight(a_w11, true); c21.setWeight(a_w21, true); c31.setWeight(a_w31, true); c41.setWeight(a_w41, true);
        c12.setWeight(a_w12, true); c22.setWeight(a_w22, true); c32.setWeight(a_w32, true); c42.setWeight(a_w42, true);
        c13.setWeight(a_w13, true); c23.setWeight(a_w23, true); c33.setWeight(a_w33, true); c43.setWeight(a_w43, true);
        c11.setWeight(c_w11, false); c21.setWeight(c_w21, false); c31.setWeight(c_w31, false); c41.setWeight(c_w41, false);
        c12.setWeight(c_w12, false); c22.setWeight(c_w22, false); c32.setWeight(c_w32, false); c42.setWeight(c_w42, false);
        c13.setWeight(c_w13, false); c23.setWeight(c_w23, false); c33.setWeight(c_w33, false); c43.setWeight(c_w43, false);
        c11.setIN(1); c21.setIN(2); c31.setIN(3); c41.setIN(4);
        c12.setIN(5); c22.setIN(6); c32.setIN(7); c42.setIN(8);
        c13.setIN(9); c23.setIN(10); c33.setIN(11); c43.setIN(12);
        out1.getInCons().addAll(Arrays.asList(c11, c21, c31, c41));
        out2.getInCons().addAll(Arrays.asList(c12, c22, c32, c42));
        out3.getInCons().addAll(Arrays.asList(c13, c23, c33, c43));
        g.getCons().addAll(c11, c21, c31, c41, c12, c22, c32, c42, c13, c23, c33, c43);
        g.express();

        // TEST FORWARD METHOD
        assertArrayEquals(y_actor, g.feed(new double[]{x1, x2, x3, x4}, true));
        assertArrayEquals(y_critic, g.feed(new double[]{x1, x2, x3, x4}, false));

        // TEST BACK PROPAGATION METHOD
        g.train(new double[]{x1, x2, x3, x4}, a_dL_dy[c], c, true);

        assertDoubleApprox(a_dL_dx1, g.getMRUActorGrad().get(1));
        assertDoubleApprox(a_dL_dx2, g.getMRUActorGrad().get(2));
        assertDoubleApprox(a_dL_dx3, g.getMRUActorGrad().get(3));
        assertDoubleApprox(a_dL_dx4, g.getMRUActorGrad().get(4));

        assertDoubleApprox(a_w11 + a_dL_dw11, c11.getWeight(true));
        assertDoubleApprox(a_w21 + a_dL_dw21, c21.getWeight(true));
        assertDoubleApprox(a_w31 + a_dL_dw31, c31.getWeight(true));
        assertDoubleApprox(a_w41 + a_dL_dw41, c41.getWeight(true));

        assertDoubleApprox(a_w12 + a_dL_dw12, c12.getWeight(true));
        assertDoubleApprox(a_w22 + a_dL_dw22, c22.getWeight(true));
        assertDoubleApprox(a_w32 + a_dL_dw32, c32.getWeight(true));
        assertDoubleApprox(a_w42 + a_dL_dw42, c42.getWeight(true));

        g.train(new double[]{x1, x2, x3, x4}, c_dL_dy, -1, false);

        assertDoubleApprox(c_dL_dx1, g.getMRUCriticGrad().get(1));
        assertDoubleApprox(c_dL_dx2, g.getMRUCriticGrad().get(2));
        assertDoubleApprox(c_dL_dx3, g.getMRUCriticGrad().get(3));
        assertDoubleApprox(c_dL_dx4, g.getMRUCriticGrad().get(4));

        assertDoubleApprox(c_w13 + c_dL_dw13, c13.getWeight(false));
        assertDoubleApprox(c_w23 + c_dL_dw23, c23.getWeight(false));
        assertDoubleApprox(c_w33 + c_dL_dw33, c33.getWeight(false));
        assertDoubleApprox(c_w43 + c_dL_dw43, c43.getWeight(false));

    }

    @RepeatedTest(100)
    public void testDeepDense() {

        // VALUE SET UP
        double x1 = r(), x2 = r(), x3 = r(), x4 = r();
        double c_w11h = r(), c_w21h = r(), c_w31h = r(), c_w41h = r();
        double c_w12h = r(), c_w22h = r(), c_w32h = r(), c_w42h = r();
        double a_w11h = r(), a_w21h = r(), a_w31h = r(), a_w41h = r();
        double a_w12h = r(), a_w22h = r(), a_w32h = r(), a_w42h = r();
        double c_w1h1 = r(), c_w2h1 = r();
        double c_w1h2 = r(), c_w2h2 = r();
        double c_w1h3 = r(), c_w2h3 = r();
        double a_w1h1 = r(), a_w2h1 = r();
        double a_w1h2 = r(), a_w2h2 = r();
        double a_w1h3 = r(), a_w2h3 = r();

        // TRUE VALUE CALCULATION
        // FORWARDING
        double c_h1 = sigmoid(x1 * c_w11h + x2 * c_w21h + x3 * c_w31h + x4 * c_w41h);
        double c_h2 = sigmoid(x1 * c_w12h + x2 * c_w22h + x3 * c_w32h + x4 * c_w42h);
        double a_h1 = sigmoid(x1 * a_w11h + x2 * a_w21h + x3 * a_w31h + x4 * a_w41h);
        double a_h2 = sigmoid(x1 * a_w12h + x2 * a_w22h + x3 * a_w32h + x4 * a_w42h);

        double a_z1 = a_h1 * a_w1h1 + a_h2 * a_w2h1;
        double a_z2 = a_h1 * a_w1h2 + a_h2 * a_w2h2;

        double[] y_actor = softmax(new double[]{a_z1, a_z2});

        double c_z3 = c_h1 * c_w1h3 + c_h2 * c_w2h3;

        double[] y_critic = new double[]{c_z3};

        // BACK PROPAGATING
        int c = stochasticSelection(y_actor);
        double v = y_critic[0];
        double reward = r() * 10; // assume environment return

        double[] a_dL_dy = new double[2];
        a_dL_dy[c] = (reward - v) / y_actor[c];
        double a_dL_dy1 = a_dL_dy[0];
        double a_dL_dy2 = a_dL_dy[1];

        double exp_z1 = Math.exp(a_z1);
        double exp_z2 = Math.exp(a_z2);
        double S = exp_z1 + exp_z2;
        double a_dL_dz1 = a_dL_dy1 * (exp_z1 * (S - exp_z1))/Math.pow(S, 2) + a_dL_dy2 * (-exp_z2*exp_z1)/Math.pow(S, 2);
        double a_dL_dw1h1 = a_dL_dz1 * a_h1 * ACU_LR;
        double a_dL_dw2h1 = a_dL_dz1 * a_h2 * ACU_LR;
        double a_dL_dz2 = a_dL_dy2 * (exp_z2 * (S - exp_z2))/Math.pow(S, 2) + a_dL_dy1 * (-exp_z1*exp_z2)/Math.pow(S, 2);
        double a_dL_dw1h2 = a_dL_dz2 * a_h1 * ACU_LR;
        double a_dL_dw2h2 = a_dL_dz2 * a_h2 * ACU_LR;

        double a_dL_dyz1 = a_dL_dz1 * a_w1h1 + a_dL_dz2 * a_w1h2;
        double a_dL_dzz1 = a_dL_dyz1 * d_sigmoid(a_h1);
        double a_dL_dw11h = a_dL_dzz1 * x1 * ACU_LR;
        double a_dL_dw21h = a_dL_dzz1 * x2 * ACU_LR;
        double a_dL_dw31h = a_dL_dzz1 * x3 * ACU_LR;
        double a_dL_dw41h = a_dL_dzz1 * x4 * ACU_LR;

        double a_dL_dyz2 = a_dL_dz1 * a_w2h1 + a_dL_dz2 * a_w2h2;
        double a_dL_dzz2 = a_dL_dyz2 * d_sigmoid(a_h2);
        double a_dL_dw12h = a_dL_dzz2 * x1 * ACU_LR;
        double a_dL_dw22h = a_dL_dzz2 * x2 * ACU_LR;
        double a_dL_dw32h = a_dL_dzz2 * x3 * ACU_LR;
        double a_dL_dw42h = a_dL_dzz2 * x4 * ACU_LR;

        double a_dL_dx1 = a_dL_dzz1 * a_w11h + a_dL_dzz2 * a_w12h;
        double a_dL_dx2 = a_dL_dzz1 * a_w21h + a_dL_dzz2 * a_w22h;
        double a_dL_dx3 = a_dL_dzz1 * a_w31h + a_dL_dzz2 * a_w32h;
        double a_dL_dx4 = a_dL_dzz1 * a_w41h + a_dL_dzz2 * a_w42h;

        double c_dL_dy = reward - v;

        double c_dL_dz3 = c_dL_dy * 1;
        double c_dL_dw1h3 = c_dL_dz3 * c_h1 * ACU_LR;
        double c_dL_dw2h3 = c_dL_dz3 * c_h2 * ACU_LR;

        double c_dL_dyz1 = c_dL_dz3 * c_w1h3;
        double c_dL_dzz1 = c_dL_dyz1 * d_sigmoid(c_h1);
        double c_dL_dw11h = c_dL_dzz1 * x1 * ACU_LR;
        double c_dL_dw21h = c_dL_dzz1 * x2 * ACU_LR;
        double c_dL_dw31h = c_dL_dzz1 * x3 * ACU_LR;
        double c_dL_dw41h = c_dL_dzz1 * x4 * ACU_LR;

        double c_dL_dyz2 = c_dL_dz3 * c_w2h3;
        double c_dL_dzz2 = c_dL_dyz2 * d_sigmoid(c_h2);
        double c_dL_dw12h = c_dL_dzz2 * x1 * ACU_LR;
        double c_dL_dw22h = c_dL_dzz2 * x2 * ACU_LR;
        double c_dL_dw32h = c_dL_dzz2 * x3 * ACU_LR;
        double c_dL_dw42h = c_dL_dzz2 * x4 * ACU_LR;

        double c_dL_dx1 = c_dL_dzz1 * c_w11h + c_dL_dzz2 * c_w12h;
        double c_dL_dx2 = c_dL_dzz1 * c_w21h + c_dL_dzz2 * c_w22h;
        double c_dL_dx3 = c_dL_dzz1 * c_w31h + c_dL_dzz2 * c_w32h;
        double c_dL_dx4 = c_dL_dzz1 * c_w41h + c_dL_dzz2 * c_w42h;

        // GENOME SET UP
        double midX = (hiddenNodeX + outputNodeX) / 2.0;
        ACUNodeGene hid1 = new ACUNodeGene(8), hid2 = new ACUNodeGene(9);
        hid1.setX(midX); hid2.setX(midX);
        g.getNodes().addAll(hid1, hid2);

        ACUConGene c11h = new ACUConGene(in1, hid1), c21h = new ACUConGene(in2, hid1), c31h = new ACUConGene(in3, hid1), c41h = new ACUConGene(in4, hid1);
        ACUConGene c12h = new ACUConGene(in1, hid2), c22h = new ACUConGene(in2, hid2), c32h = new ACUConGene(in3, hid2), c42h = new ACUConGene(in4, hid2);
        ACUConGene c1h1 = new ACUConGene(hid1, out1), c2h1 = new ACUConGene(hid2, out1);
        ACUConGene c1h2 = new ACUConGene(hid1, out2), c2h2 = new ACUConGene(hid2, out2);
        ACUConGene c1h3 = new ACUConGene(hid1, out3), c2h3 = new ACUConGene(hid2, out3);
        c11h.setWeight(a_w11h, true); c21h.setWeight(a_w21h, true); c31h.setWeight(a_w31h, true); c41h.setWeight(a_w41h, true);
        c12h.setWeight(a_w12h, true); c22h.setWeight(a_w22h, true); c32h.setWeight(a_w32h, true); c42h.setWeight(a_w42h, true);
        c1h1.setWeight(a_w1h1, true); c2h1.setWeight(a_w2h1, true);
        c1h2.setWeight(a_w1h2, true); c2h2.setWeight(a_w2h2, true);
        c1h3.setWeight(a_w1h3, true); c2h3.setWeight(a_w2h3, true);
        c11h.setWeight(c_w11h, false); c21h.setWeight(c_w21h, false); c31h.setWeight(c_w31h, false); c41h.setWeight(c_w41h, false);
        c12h.setWeight(c_w12h, false); c22h.setWeight(c_w22h, false); c32h.setWeight(c_w32h, false); c42h.setWeight(c_w42h, false);
        c1h1.setWeight(c_w1h1, false); c2h1.setWeight(c_w2h1, false);
        c1h2.setWeight(c_w1h2, false); c2h2.setWeight(c_w2h2, false);
        c1h3.setWeight(c_w1h3, false); c2h3.setWeight(c_w2h3, false);
        c11h.setIN(1); c21h.setIN(2); c31h.setIN(3); c41h.setIN(4);
        c12h.setIN(5); c22h.setIN(6); c32h.setIN(7); c42h.setIN(8);
        c1h1.setIN(9); c2h1.setIN(10);
        c1h2.setIN(11); c2h2.setIN(12);
        c1h3.setIN(13); c2h3.setIN(14);
        hid1.getInCons().addAll(Arrays.asList(c11h, c21h, c31h, c41h));
        hid2.getInCons().addAll(Arrays.asList(c12h, c22h, c32h, c42h));
        out1.getInCons().addAll(Arrays.asList(c1h1, c2h1));
        out2.getInCons().addAll(Arrays.asList(c1h2, c2h2));
        out3.getInCons().addAll(Arrays.asList(c1h3, c2h3));
        g.getCons().addAll(c11h, c21h, c31h, c41h, c12h, c22h, c32h, c42h, c1h1, c2h1, c1h2, c2h2, c1h3, c2h3);
        g.express();

        // TEST FORWARD METHOD
        assertArrayEquals(y_actor, g.feed(new double[]{x1, x2, x3, x4}, true));
        assertArrayEquals(y_critic, g.feed(new double[]{x1, x2, x3, x4}, false));

        // TEST BACK PROPAGATION METHOD
        g.train(new double[]{x1, x2, x3, x4}, a_dL_dy[c], c, true);

        assertDoubleApprox(a_dL_dx1, g.getMRUActorGrad().get(1));
        assertDoubleApprox(a_dL_dx2, g.getMRUActorGrad().get(2));
        assertDoubleApprox(a_dL_dx3, g.getMRUActorGrad().get(3));
        assertDoubleApprox(a_dL_dx4, g.getMRUActorGrad().get(4));

        assertDoubleApprox(a_w1h1 + a_dL_dw1h1, c1h1.getWeight(true));
        assertDoubleApprox(a_w2h1 + a_dL_dw2h1, c2h1.getWeight(true));

        assertDoubleApprox(a_w1h2 + a_dL_dw1h2, c1h2.getWeight(true));
        assertDoubleApprox(a_w2h2 + a_dL_dw2h2, c2h2.getWeight(true));

        assertDoubleApprox(a_w11h + a_dL_dw11h, c11h.getWeight(true));
        assertDoubleApprox(a_w21h + a_dL_dw21h, c21h.getWeight(true));
        assertDoubleApprox(a_w31h + a_dL_dw31h, c31h.getWeight(true));
        assertDoubleApprox(a_w41h + a_dL_dw41h, c41h.getWeight(true));

        assertDoubleApprox(a_w12h + a_dL_dw12h, c12h.getWeight(true));
        assertDoubleApprox(a_w22h + a_dL_dw22h, c22h.getWeight(true));
        assertDoubleApprox(a_w32h + a_dL_dw32h, c32h.getWeight(true));
        assertDoubleApprox(a_w42h + a_dL_dw42h, c42h.getWeight(true));

        g.train(new double[]{x1, x2, x3, x4}, c_dL_dy, -1, false);

        assertDoubleApprox(c_dL_dx1, g.getMRUCriticGrad().get(1));
        assertDoubleApprox(c_dL_dx2, g.getMRUCriticGrad().get(2));
        assertDoubleApprox(c_dL_dx3, g.getMRUCriticGrad().get(3));
        assertDoubleApprox(c_dL_dx4, g.getMRUCriticGrad().get(4));

        assertDoubleApprox(c_w1h3 + c_dL_dw1h3, c1h3.getWeight(false));
        assertDoubleApprox(c_w2h3 + c_dL_dw2h3, c2h3.getWeight(false));

        assertDoubleApprox(c_w11h + c_dL_dw11h, c11h.getWeight(false));
        assertDoubleApprox(c_w21h + c_dL_dw21h, c21h.getWeight(false));
        assertDoubleApprox(c_w31h + c_dL_dw31h, c31h.getWeight(false));
        assertDoubleApprox(c_w41h + c_dL_dw41h, c41h.getWeight(false));

        assertDoubleApprox(c_w12h + c_dL_dw12h, c12h.getWeight(false));
        assertDoubleApprox(c_w22h + c_dL_dw22h, c22h.getWeight(false));
        assertDoubleApprox(c_w32h + c_dL_dw32h, c32h.getWeight(false));
        assertDoubleApprox(c_w42h + c_dL_dw42h, c42h.getWeight(false));

    }

}
