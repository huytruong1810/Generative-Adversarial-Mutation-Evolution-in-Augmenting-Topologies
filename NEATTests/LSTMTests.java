package NEATTests;

import NEAT.Genome.MRU.MRUConGene;
import NEAT.Genome.MRU.MRUGenome;
import NEAT.Genome.MRU.MRUNodeGene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static NEAT.NEAT.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class LSTMTests {

    MRUGenome g;
    MRUNodeGene in1, in2, in3, in4, in5, in6, in7;
    MRUNodeGene out1, out2, out3;

    @BeforeEach
    public void setUp() {

        g = new MRUGenome(7, 3);

        in1 = new MRUNodeGene(1); in2 = new MRUNodeGene(2); in3 = new MRUNodeGene(3); in4 = new MRUNodeGene(4);
        in5 = new MRUNodeGene(5); in6 = new MRUNodeGene(6); in7 = new MRUNodeGene(7);
        out1 = new MRUNodeGene(8); out2 = new MRUNodeGene(9); out3 = new MRUNodeGene(10);

        in1.setX(inputNodeX); in2.setX(inputNodeX); in3.setX(inputNodeX); in4.setX(inputNodeX);
        in5.setX(inputNodeX); in6.setX(inputNodeX); in7.setX(inputNodeX);
        out1.setX(hiddenNodeX); out2.setX(hiddenNodeX); out3.setX(hiddenNodeX);

        g.getNodes().addAll(in1, in2, in3, in4,  in5, in6, in7, out1, out2, out3);

    }

    private double r() { return Math.random() * 2 - 1; }

    @RepeatedTest(10)
    public void testBasicForward() {

        double x1_t1 = r(), x2_t1 = r(), x3_t1 = r(), x4_t1 = r();
        double x1_t2 = r(), x2_t2 = r(), x3_t2 = r(), x4_t2 = r();

        double[] y = new double[]{0, 0, 0};

        g.express();

        ArrayList<double[]> inputSeq = new ArrayList<>();
        inputSeq.add(new double[]{x1_t1, x2_t1, x3_t1, x4_t1});
        inputSeq.add(new double[]{x1_t2, x2_t2, x3_t2, x4_t2});
        double[] y_pred = g.feed(inputSeq);

        assertArrayEquals(y, y_pred);

    }

    private double tanh(double x) { return 2 / (1 + Math.exp(-2 * x)) - 1;}

    private double d_tanh(double t) { return 1 - Math.pow(t, 2); }

    private double sigmoid(double x) { return 1 / (1 + Math.exp(-x)); }

    private double d_sigmoid(double s) { return s * (1 - s); }

    private void assertDoubleApprox(double expected, double actual) {
        double f = Math.pow(10, 12);
        assertEquals( Math.round(expected * f) / f, Math.round(actual * f) / f);
    }

    @RepeatedTest(100)
    public void testDense() {

        // VALUE SET UP
        double x1_t1 = r(), x2_t1 = r(), x3_t1 = r(), x4_t1 = r();
        double x1_t2 = r(), x2_t2 = r(), x3_t2 = r(), x4_t2 = r();
        double f_w11 = r(), f_w21 = r(), f_w31 = r(), f_w41 = r(), f_w51 = r(), f_w61 = r(), f_w71 = r();
        double f_w12 = r(), f_w22 = r(), f_w32 = r(), f_w42 = r(), f_w52 = r(), f_w62 = r(), f_w72 = r();
        double f_w13 = r(), f_w23 = r(), f_w33 = r(), f_w43 = r(), f_w53 = r(), f_w63 = r(), f_w73 = r();
        double i_w11 = r(), i_w21 = r(), i_w31 = r(), i_w41 = r(), i_w51 = r(), i_w61 = r(), i_w71 = r();
        double i_w12 = r(), i_w22 = r(), i_w32 = r(), i_w42 = r(), i_w52 = r(), i_w62 = r(), i_w72 = r();
        double i_w13 = r(), i_w23 = r(), i_w33 = r(), i_w43 = r(), i_w53 = r(), i_w63 = r(), i_w73 = r();
        double c_w11 = r(), c_w21 = r(), c_w31 = r(), c_w41 = r(), c_w51 = r(), c_w61 = r(), c_w71 = r();
        double c_w12 = r(), c_w22 = r(), c_w32 = r(), c_w42 = r(), c_w52 = r(), c_w62 = r(), c_w72 = r();
        double c_w13 = r(), c_w23 = r(), c_w33 = r(), c_w43 = r(), c_w53 = r(), c_w63 = r(), c_w73 = r();
        double o_w11 = r(), o_w21 = r(), o_w31 = r(), o_w41 = r(), o_w51 = r(), o_w61 = r(), o_w71 = r();
        double o_w12 = r(), o_w22 = r(), o_w32 = r(), o_w42 = r(), o_w52 = r(), o_w62 = r(), o_w72 = r();
        double o_w13 = r(), o_w23 = r(), o_w33 = r(), o_w43 = r(), o_w53 = r(), o_w63 = r(), o_w73 = r();

        ArrayList<double[]> inputSeq = new ArrayList<>(Arrays.asList(
                new double[]{x1_t1, x2_t1, x3_t1, x4_t1},
                new double[]{x1_t2, x2_t2, x3_t2, x4_t2}
        ));

        // TRUE VALUE CALCULATION
        // FORWARDING
        double C1_t0 = 0, C2_t0 = 0, C3_t0 = 0;
        double h1_t0 = 0, h2_t0 = 0, h3_t0 = 0;
        // forget gate at time step 1
        double f1_t1 = sigmoid(x1_t1 * f_w11 + x2_t1 * f_w21 + x3_t1 * f_w31 + x4_t1 * f_w41 + h1_t0 * f_w51 + h2_t0 * f_w61 + h3_t0 * f_w71);
        double f2_t1 = sigmoid(x1_t1 * f_w12 + x2_t1 * f_w22 + x3_t1 * f_w32 + x4_t1 * f_w42 + h1_t0 * f_w52 + h2_t0 * f_w62 + h3_t0 * f_w72);
        double f3_t1 = sigmoid(x1_t1 * f_w13 + x2_t1 * f_w23 + x3_t1 * f_w33 + x4_t1 * f_w43 + h1_t0 * f_w53 + h2_t0 * f_w63 + h3_t0 * f_w73);
        // input gate at time step 1
        double i1_t1 = sigmoid(x1_t1 * i_w11 + x2_t1 * i_w21 + x3_t1 * i_w31 + x4_t1 * i_w41 + h1_t0 * i_w51 + h2_t0 * i_w61 + h3_t0 * i_w71);
        double i2_t1 = sigmoid(x1_t1 * i_w12 + x2_t1 * i_w22 + x3_t1 * i_w32 + x4_t1 * i_w42 + h1_t0 * i_w52 + h2_t0 * i_w62 + h3_t0 * i_w72);
        double i3_t1 = sigmoid(x1_t1 * i_w13 + x2_t1 * i_w23 + x3_t1 * i_w33 + x4_t1 * i_w43 + h1_t0 * i_w53 + h2_t0 * i_w63 + h3_t0 * i_w73);
        // candidate gate at time step 1
        double c1_t1 = tanh(x1_t1 * c_w11 + x2_t1 * c_w21 + x3_t1 * c_w31 + x4_t1 * c_w41 + h1_t0 * c_w51 + h2_t0 * c_w61 + h3_t0 * c_w71);
        double c2_t1 = tanh(x1_t1 * c_w12 + x2_t1 * c_w22 + x3_t1 * c_w32 + x4_t1 * c_w42 + h1_t0 * c_w52 + h2_t0 * c_w62 + h3_t0 * c_w72);
        double c3_t1 = tanh(x1_t1 * c_w13 + x2_t1 * c_w23 + x3_t1 * c_w33 + x4_t1 * c_w43 + h1_t0 * c_w53 + h2_t0 * c_w63 + h3_t0 * c_w73);
        // output gate at time step 1
        double o1_t1 = sigmoid(x1_t1 * o_w11 + x2_t1 * o_w21 + x3_t1 * o_w31 + x4_t1 * o_w41 + h1_t0 * o_w51 + h2_t0 * o_w61 + h3_t0 * o_w71);
        double o2_t1 = sigmoid(x1_t1 * o_w12 + x2_t1 * o_w22 + x3_t1 * o_w32 + x4_t1 * o_w42 + h1_t0 * o_w52 + h2_t0 * o_w62 + h3_t0 * o_w72);
        double o3_t1 = sigmoid(x1_t1 * o_w13 + x2_t1 * o_w23 + x3_t1 * o_w33 + x4_t1 * o_w43 + h1_t0 * o_w53 + h2_t0 * o_w63 + h3_t0 * o_w73);
        // cell state at time step 1
        double C1_t1 = f1_t1 * C1_t0 + i1_t1 * c1_t1;
        double C2_t1 = f2_t1 * C2_t0 + i2_t1 * c2_t1;
        double C3_t1 = f3_t1 * C3_t0 + i3_t1 * c3_t1;
        // hidden state at time step 1
        double h1_t1 = o1_t1 * tanh(C1_t1);
        double h2_t1 = o2_t1 * tanh(C2_t1);
        double h3_t1 = o3_t1 * tanh(C3_t1);
        // forget gate at time step 2
        double f1_t2 = sigmoid(x1_t2 * f_w11 + x2_t2 * f_w21 + x3_t2 * f_w31 + x4_t2 * f_w41 + h1_t1 * f_w51 + h2_t1 * f_w61 + h3_t1 * f_w71);
        double f2_t2 = sigmoid(x1_t2 * f_w12 + x2_t2 * f_w22 + x3_t2 * f_w32 + x4_t2 * f_w42 + h1_t1 * f_w52 + h2_t1 * f_w62 + h3_t1 * f_w72);
        double f3_t2 = sigmoid(x1_t2 * f_w13 + x2_t2 * f_w23 + x3_t2 * f_w33 + x4_t2 * f_w43 + h1_t1 * f_w53 + h2_t1 * f_w63 + h3_t1 * f_w73);
        // input gate at time step 2
        double i1_t2 = sigmoid(x1_t2 * i_w11 + x2_t2 * i_w21 + x3_t2 * i_w31 + x4_t2 * i_w41 + h1_t1 * i_w51 + h2_t1 * i_w61 + h3_t1 * i_w71);
        double i2_t2 = sigmoid(x1_t2 * i_w12 + x2_t2 * i_w22 + x3_t2 * i_w32 + x4_t2 * i_w42 + h1_t1 * i_w52 + h2_t1 * i_w62 + h3_t1 * i_w72);
        double i3_t2 = sigmoid(x1_t2 * i_w13 + x2_t2 * i_w23 + x3_t2 * i_w33 + x4_t2 * i_w43 + h1_t1 * i_w53 + h2_t1 * i_w63 + h3_t1 * i_w73);
        // candidate gate at time step 2
        double c1_t2 = tanh(x1_t2 * c_w11 + x2_t2 * c_w21 + x3_t2 * c_w31 + x4_t2 * c_w41 + h1_t1 * c_w51 + h2_t1 * c_w61 + h3_t1 * c_w71);
        double c2_t2 = tanh(x1_t2 * c_w12 + x2_t2 * c_w22 + x3_t2 * c_w32 + x4_t2 * c_w42 + h1_t1 * c_w52 + h2_t1 * c_w62 + h3_t1 * c_w72);
        double c3_t2 = tanh(x1_t2 * c_w13 + x2_t2 * c_w23 + x3_t2 * c_w33 + x4_t2 * c_w43 + h1_t1 * c_w53 + h2_t1 * c_w63 + h3_t1 * c_w73);
        // output gate at time step 2
        double o1_t2 = sigmoid(x1_t2 * o_w11 + x2_t2 * o_w21 + x3_t2 * o_w31 + x4_t2 * o_w41 + h1_t1 * o_w51 + h2_t1 * o_w61 + h3_t1 * o_w71);
        double o2_t2 = sigmoid(x1_t2 * o_w12 + x2_t2 * o_w22 + x3_t2 * o_w32 + x4_t2 * o_w42 + h1_t1 * o_w52 + h2_t1 * o_w62 + h3_t1 * o_w72);
        double o3_t2 = sigmoid(x1_t2 * o_w13 + x2_t2 * o_w23 + x3_t2 * o_w33 + x4_t2 * o_w43 + h1_t1 * o_w53 + h2_t1 * o_w63 + h3_t1 * o_w73);
        // cell state at time step 2
        double C1_t2 = f1_t2 * C1_t1 + i1_t2 * c1_t2;
        double C2_t2 = f2_t2 * C2_t1 + i2_t2 * c2_t2;
        double C3_t2 = f3_t2 * C3_t1 + i3_t2 * c3_t2;
        // hidden state at time step 2
        double h1_t2 = o1_t2 * tanh(C1_t2);
        double h2_t2 = o2_t2 * tanh(C2_t2);
        double h3_t2 = o3_t2 * tanh(C3_t2);

        double[] y = new double[]{h1_t2, h2_t2, h3_t2};

        // BACK PROPAGATING
        double a_dL_dh1_t2 = r(), a_dL_dh2_t2 = r(), a_dL_dh3_t2 = r(); // assume actor gradient
        double c_dL_dh1_t2 = r(), c_dL_dh2_t2 = r(), c_dL_dh3_t2 = r(); // assume critic gradient
        HashMap<Integer, Double> actorGrads  = new HashMap<>(){{ put(1, a_dL_dh1_t2); put(2, a_dL_dh2_t2); put(3, a_dL_dh3_t2); }};
        HashMap<Integer, Double> criticGrads  = new HashMap<>(){{ put(1, c_dL_dh1_t2); put(2, c_dL_dh2_t2); put(3, c_dL_dh3_t2); }};

        double dL_dh1_t2 = a_dL_dh1_t2 + c_dL_dh1_t2;
        double dL_dh2_t2 = a_dL_dh2_t2 + c_dL_dh2_t2;
        double dL_dh3_t2 = a_dL_dh3_t2 + c_dL_dh3_t2;

        // at time 2
        double dL_dC1_t2 = dL_dh1_t2 * o1_t2 * (1 - Math.pow(tanh(C1_t2), 2));
        double dL_dC2_t2 = dL_dh2_t2 * o2_t2 * (1 - Math.pow(tanh(C2_t2), 2));
        double dL_dC3_t2 = dL_dh3_t2 * o3_t2 * (1 - Math.pow(tanh(C3_t2), 2));

        double dL_do1_t2 = dL_dh1_t2 * tanh(C1_t2), dL_do2_t2 = dL_dh2_t2 * tanh(C2_t2), dL_do3_t2 = dL_dh3_t2 * tanh(C3_t2);
        double dL_df1_t2 = dL_dC1_t2 * C1_t1, dL_df2_t2 = dL_dC2_t2 * C2_t1, dL_df3_t2 = dL_dC3_t2 * C3_t1;
        double dL_di1_t2 = dL_dC1_t2 * c1_t2, dL_di2_t2 = dL_dC2_t2 * c2_t2, dL_di3_t2 = dL_dC3_t2 * c3_t2;
        double dL_dc1_t2 = dL_dC1_t2 * i1_t2, dL_dc2_t2 = dL_dC2_t2 * i2_t2, dL_dc3_t2 = dL_dC3_t2 * i3_t2;

        double dL_dwo11_t2 = dL_do1_t2 * d_sigmoid(o1_t2) * x1_t2 * MRU_LR;
        double dL_dwo21_t2 = dL_do1_t2 * d_sigmoid(o1_t2) * x2_t2 * MRU_LR;
        double dL_dwo31_t2 = dL_do1_t2 * d_sigmoid(o1_t2) * x3_t2 * MRU_LR;
        double dL_dwo41_t2 = dL_do1_t2 * d_sigmoid(o1_t2) * x4_t2 * MRU_LR;
        double dL_dwo51_t2 = dL_do1_t2 * d_sigmoid(o1_t2) * h1_t1 * MRU_LR;
        double dL_dwo61_t2 = dL_do1_t2 * d_sigmoid(o1_t2) * h2_t1 * MRU_LR;
        double dL_dwo71_t2 = dL_do1_t2 * d_sigmoid(o1_t2) * h3_t1 * MRU_LR;

        double dL_dwo12_t2 = dL_do2_t2 * d_sigmoid(o2_t2) * x1_t2 * MRU_LR;
        double dL_dwo22_t2 = dL_do2_t2 * d_sigmoid(o2_t2) * x2_t2 * MRU_LR;
        double dL_dwo32_t2 = dL_do2_t2 * d_sigmoid(o2_t2) * x3_t2 * MRU_LR;
        double dL_dwo42_t2 = dL_do2_t2 * d_sigmoid(o2_t2) * x4_t2 * MRU_LR;
        double dL_dwo52_t2 = dL_do2_t2 * d_sigmoid(o2_t2) * h1_t1 * MRU_LR;
        double dL_dwo62_t2 = dL_do2_t2 * d_sigmoid(o2_t2) * h2_t1 * MRU_LR;
        double dL_dwo72_t2 = dL_do2_t2 * d_sigmoid(o2_t2) * h3_t1 * MRU_LR;

        double dL_dwo13_t2 = dL_do3_t2 * d_sigmoid(o3_t2) * x1_t2 * MRU_LR;
        double dL_dwo23_t2 = dL_do3_t2 * d_sigmoid(o3_t2) * x2_t2 * MRU_LR;
        double dL_dwo33_t2 = dL_do3_t2 * d_sigmoid(o3_t2) * x3_t2 * MRU_LR;
        double dL_dwo43_t2 = dL_do3_t2 * d_sigmoid(o3_t2) * x4_t2 * MRU_LR;
        double dL_dwo53_t2 = dL_do3_t2 * d_sigmoid(o3_t2) * h1_t1 * MRU_LR;
        double dL_dwo63_t2 = dL_do3_t2 * d_sigmoid(o3_t2) * h2_t1 * MRU_LR;
        double dL_dwo73_t2 = dL_do3_t2 * d_sigmoid(o3_t2) * h3_t1 * MRU_LR;

        double dL_dwc11_t2 = dL_dc1_t2 * d_tanh(c1_t2) * x1_t2 * MRU_LR;
        double dL_dwc21_t2 = dL_dc1_t2 * d_tanh(c1_t2) * x2_t2 * MRU_LR;
        double dL_dwc31_t2 = dL_dc1_t2 * d_tanh(c1_t2) * x3_t2 * MRU_LR;
        double dL_dwc41_t2 = dL_dc1_t2 * d_tanh(c1_t2) * x4_t2 * MRU_LR;
        double dL_dwc51_t2 = dL_dc1_t2 * d_tanh(c1_t2) * h1_t1 * MRU_LR;
        double dL_dwc61_t2 = dL_dc1_t2 * d_tanh(c1_t2) * h2_t1 * MRU_LR;
        double dL_dwc71_t2 = dL_dc1_t2 * d_tanh(c1_t2) * h3_t1 * MRU_LR;

        double dL_dwc12_t2 = dL_dc2_t2 * d_tanh(c2_t2) * x1_t2 * MRU_LR;
        double dL_dwc22_t2 = dL_dc2_t2 * d_tanh(c2_t2) * x2_t2 * MRU_LR;
        double dL_dwc32_t2 = dL_dc2_t2 * d_tanh(c2_t2) * x3_t2 * MRU_LR;
        double dL_dwc42_t2 = dL_dc2_t2 * d_tanh(c2_t2) * x4_t2 * MRU_LR;
        double dL_dwc52_t2 = dL_dc2_t2 * d_tanh(c2_t2) * h1_t1 * MRU_LR;
        double dL_dwc62_t2 = dL_dc2_t2 * d_tanh(c2_t2) * h2_t1 * MRU_LR;
        double dL_dwc72_t2 = dL_dc2_t2 * d_tanh(c2_t2) * h3_t1 * MRU_LR;

        double dL_dwc13_t2 = dL_dc3_t2 * d_tanh(c3_t2) * x1_t2 * MRU_LR;
        double dL_dwc23_t2 = dL_dc3_t2 * d_tanh(c3_t2) * x2_t2 * MRU_LR;
        double dL_dwc33_t2 = dL_dc3_t2 * d_tanh(c3_t2) * x3_t2 * MRU_LR;
        double dL_dwc43_t2 = dL_dc3_t2 * d_tanh(c3_t2) * x4_t2 * MRU_LR;
        double dL_dwc53_t2 = dL_dc3_t2 * d_tanh(c3_t2) * h1_t1 * MRU_LR;
        double dL_dwc63_t2 = dL_dc3_t2 * d_tanh(c3_t2) * h2_t1 * MRU_LR;
        double dL_dwc73_t2 = dL_dc3_t2 * d_tanh(c3_t2) * h3_t1 * MRU_LR;

        double dL_dwi11_t2 = dL_di1_t2 * d_sigmoid(i1_t2) * x1_t2 * MRU_LR;
        double dL_dwi21_t2 = dL_di1_t2 * d_sigmoid(i1_t2) * x2_t2 * MRU_LR;
        double dL_dwi31_t2 = dL_di1_t2 * d_sigmoid(i1_t2) * x3_t2 * MRU_LR;
        double dL_dwi41_t2 = dL_di1_t2 * d_sigmoid(i1_t2) * x4_t2 * MRU_LR;
        double dL_dwi51_t2 = dL_di1_t2 * d_sigmoid(i1_t2) * h1_t1 * MRU_LR;
        double dL_dwi61_t2 = dL_di1_t2 * d_sigmoid(i1_t2) * h2_t1 * MRU_LR;
        double dL_dwi71_t2 = dL_di1_t2 * d_sigmoid(i1_t2) * h3_t1 * MRU_LR;

        double dL_dwi12_t2 = dL_di2_t2 * d_sigmoid(i2_t2) * x1_t2 * MRU_LR;
        double dL_dwi22_t2 = dL_di2_t2 * d_sigmoid(i2_t2) * x2_t2 * MRU_LR;
        double dL_dwi32_t2 = dL_di2_t2 * d_sigmoid(i2_t2) * x3_t2 * MRU_LR;
        double dL_dwi42_t2 = dL_di2_t2 * d_sigmoid(i2_t2) * x4_t2 * MRU_LR;
        double dL_dwi52_t2 = dL_di2_t2 * d_sigmoid(i2_t2) * h1_t1 * MRU_LR;
        double dL_dwi62_t2 = dL_di2_t2 * d_sigmoid(i2_t2) * h2_t1 * MRU_LR;
        double dL_dwi72_t2 = dL_di2_t2 * d_sigmoid(i2_t2) * h3_t1 * MRU_LR;

        double dL_dwi13_t2 = dL_di3_t2 * d_sigmoid(i3_t2) * x1_t2 * MRU_LR;
        double dL_dwi23_t2 = dL_di3_t2 * d_sigmoid(i3_t2) * x2_t2 * MRU_LR;
        double dL_dwi33_t2 = dL_di3_t2 * d_sigmoid(i3_t2) * x3_t2 * MRU_LR;
        double dL_dwi43_t2 = dL_di3_t2 * d_sigmoid(i3_t2) * x4_t2 * MRU_LR;
        double dL_dwi53_t2 = dL_di3_t2 * d_sigmoid(i3_t2) * h1_t1 * MRU_LR;
        double dL_dwi63_t2 = dL_di3_t2 * d_sigmoid(i3_t2) * h2_t1 * MRU_LR;
        double dL_dwi73_t2 = dL_di3_t2 * d_sigmoid(i3_t2) * h3_t1 * MRU_LR;

        double dL_dwf11_t2 = dL_df1_t2 * d_sigmoid(f1_t2) * x1_t2 * MRU_LR;
        double dL_dwf21_t2 = dL_df1_t2 * d_sigmoid(f1_t2) * x2_t2 * MRU_LR;
        double dL_dwf31_t2 = dL_df1_t2 * d_sigmoid(f1_t2) * x3_t2 * MRU_LR;
        double dL_dwf41_t2 = dL_df1_t2 * d_sigmoid(f1_t2) * x4_t2 * MRU_LR;
        double dL_dwf51_t2 = dL_df1_t2 * d_sigmoid(f1_t2) * h1_t1 * MRU_LR;
        double dL_dwf61_t2 = dL_df1_t2 * d_sigmoid(f1_t2) * h2_t1 * MRU_LR;
        double dL_dwf71_t2 = dL_df1_t2 * d_sigmoid(f1_t2) * h3_t1 * MRU_LR;

        double dL_dwf12_t2 = dL_df2_t2 * d_sigmoid(f2_t2) * x1_t2 * MRU_LR;
        double dL_dwf22_t2 = dL_df2_t2 * d_sigmoid(f2_t2) * x2_t2 * MRU_LR;
        double dL_dwf32_t2 = dL_df2_t2 * d_sigmoid(f2_t2) * x3_t2 * MRU_LR;
        double dL_dwf42_t2 = dL_df2_t2 * d_sigmoid(f2_t2) * x4_t2 * MRU_LR;
        double dL_dwf52_t2 = dL_df2_t2 * d_sigmoid(f2_t2) * h1_t1 * MRU_LR;
        double dL_dwf62_t2 = dL_df2_t2 * d_sigmoid(f2_t2) * h2_t1 * MRU_LR;
        double dL_dwf72_t2 = dL_df2_t2 * d_sigmoid(f2_t2) * h3_t1 * MRU_LR;

        double dL_dwf13_t2 = dL_df3_t2 * d_sigmoid(f3_t2) * x1_t2 * MRU_LR;
        double dL_dwf23_t2 = dL_df3_t2 * d_sigmoid(f3_t2) * x2_t2 * MRU_LR;
        double dL_dwf33_t2 = dL_df3_t2 * d_sigmoid(f3_t2) * x3_t2 * MRU_LR;
        double dL_dwf43_t2 = dL_df3_t2 * d_sigmoid(f3_t2) * x4_t2 * MRU_LR;
        double dL_dwf53_t2 = dL_df3_t2 * d_sigmoid(f3_t2) * h1_t1 * MRU_LR;
        double dL_dwf63_t2 = dL_df3_t2 * d_sigmoid(f3_t2) * h2_t1 * MRU_LR;
        double dL_dwf73_t2 = dL_df3_t2 * d_sigmoid(f3_t2) * h3_t1 * MRU_LR;

        double dL_dh1_t1 =
                dL_do1_t2 * d_sigmoid(o1_t2) * o_w51 + dL_do2_t2 * d_sigmoid(o2_t2) * o_w52 + dL_do3_t2 * d_sigmoid(o3_t2) * o_w53 +
                dL_dc1_t2 * d_tanh(c1_t2) * c_w51 + dL_dc2_t2 * d_tanh(c2_t2) * c_w52 + dL_dc3_t2 * d_tanh(c3_t2) * c_w53 +
                dL_di1_t2 * d_sigmoid(i1_t2) * i_w51 + dL_di2_t2 * d_sigmoid(i2_t2) * i_w52 + dL_di3_t2 * d_sigmoid(i3_t2) * i_w53 +
                dL_df1_t2 * d_sigmoid(f1_t2) * f_w51 + dL_df2_t2 * d_sigmoid(f2_t2) * f_w52 + dL_df3_t2 * d_sigmoid(f3_t2) * f_w53;
        double dL_dh2_t1 =
                dL_do1_t2 * d_sigmoid(o1_t2) * o_w61 + dL_do2_t2 * d_sigmoid(o2_t2) * o_w62 + dL_do3_t2 * d_sigmoid(o3_t2) * o_w63 +
                dL_dc1_t2 * d_tanh(c1_t2) * c_w61 + dL_dc2_t2 * d_tanh(c2_t2) * c_w62 + dL_dc3_t2 * d_tanh(c3_t2) * c_w63 +
                dL_di1_t2 * d_sigmoid(i1_t2) * i_w61 + dL_di2_t2 * d_sigmoid(i2_t2) * i_w62 + dL_di3_t2 * d_sigmoid(i3_t2) * i_w63 +
                dL_df1_t2 * d_sigmoid(f1_t2) * f_w61 + dL_df2_t2 * d_sigmoid(f2_t2) * f_w62 + dL_df3_t2 * d_sigmoid(f3_t2) * f_w63;
        double dL_dh3_t1 =
                dL_do1_t2 * d_sigmoid(o1_t2) * o_w71 + dL_do2_t2 * d_sigmoid(o2_t2) * o_w72 + dL_do3_t2 * d_sigmoid(o3_t2) * o_w73 +
                dL_dc1_t2 * d_tanh(c1_t2) * c_w71 + dL_dc2_t2 * d_tanh(c2_t2) * c_w72 + dL_dc3_t2 * d_tanh(c3_t2) * c_w73 +
                dL_di1_t2 * d_sigmoid(i1_t2) * i_w71 + dL_di2_t2 * d_sigmoid(i2_t2) * i_w72 + dL_di3_t2 * d_sigmoid(i3_t2) * i_w73 +
                dL_df1_t2 * d_sigmoid(f1_t2) * f_w71 + dL_df2_t2 * d_sigmoid(f2_t2) * f_w72 + dL_df3_t2 * d_sigmoid(f3_t2) * f_w73;

        double dL_dC1_t1 = dL_dC1_t2 * f1_t2;
        double dL_dC2_t1 = dL_dC2_t2 * f2_t2;
        double dL_dC3_t1 = dL_dC3_t2 * f3_t2;

        // at time 1
        dL_dC1_t1 += dL_dh1_t1 * o1_t1 * (1 - Math.pow(tanh(C1_t1), 2));
        dL_dC2_t1 += dL_dh2_t1 * o2_t1 * (1 - Math.pow(tanh(C2_t1), 2));
        dL_dC3_t1 += dL_dh3_t1 * o3_t1 * (1 - Math.pow(tanh(C3_t1), 2));

        double dL_do1_t1 = dL_dh1_t1 * tanh(C1_t1), dL_do2_t1 = dL_dh2_t1 * tanh(C2_t1), dL_do3_t1 = dL_dh3_t1 * tanh(C3_t1);
        double dL_df1_t1 = dL_dC1_t1 * C1_t0, dL_df2_t1 = dL_dC2_t1 * C2_t0, dL_df3_t1 = dL_dC3_t1 * C3_t0;
        double dL_di1_t1 = dL_dC1_t1 * c1_t1, dL_di2_t1 = dL_dC2_t1 * c2_t1, dL_di3_t1 = dL_dC3_t1 * c3_t1;
        double dL_dc1_t1 = dL_dC1_t1 * i1_t1, dL_dc2_t1 = dL_dC2_t1 * i2_t1, dL_dc3_t1 = dL_dC3_t1 * i3_t1;

        double dL_dwo11_t1 = dL_do1_t1 * d_sigmoid(o1_t1) * x1_t1 * MRU_LR;
        double dL_dwo21_t1 = dL_do1_t1 * d_sigmoid(o1_t1) * x2_t1 * MRU_LR;
        double dL_dwo31_t1 = dL_do1_t1 * d_sigmoid(o1_t1) * x3_t1 * MRU_LR;
        double dL_dwo41_t1 = dL_do1_t1 * d_sigmoid(o1_t1) * x4_t1 * MRU_LR;
        double dL_dwo51_t1 = dL_do1_t1 * d_sigmoid(o1_t1) * h1_t0 * MRU_LR;
        double dL_dwo61_t1 = dL_do1_t1 * d_sigmoid(o1_t1) * h2_t0 * MRU_LR;
        double dL_dwo71_t1 = dL_do1_t1 * d_sigmoid(o1_t1) * h3_t0 * MRU_LR;

        double dL_dwo12_t1 = dL_do2_t1 * d_sigmoid(o2_t1) * x1_t1 * MRU_LR;
        double dL_dwo22_t1 = dL_do2_t1 * d_sigmoid(o2_t1) * x2_t1 * MRU_LR;
        double dL_dwo32_t1 = dL_do2_t1 * d_sigmoid(o2_t1) * x3_t1 * MRU_LR;
        double dL_dwo42_t1 = dL_do2_t1 * d_sigmoid(o2_t1) * x4_t1 * MRU_LR;
        double dL_dwo52_t1 = dL_do2_t1 * d_sigmoid(o2_t1) * h1_t0 * MRU_LR;
        double dL_dwo62_t1 = dL_do2_t1 * d_sigmoid(o2_t1) * h2_t0 * MRU_LR;
        double dL_dwo72_t1 = dL_do2_t1 * d_sigmoid(o2_t1) * h3_t0 * MRU_LR;

        double dL_dwo13_t1 = dL_do3_t1 * d_sigmoid(o3_t1) * x1_t1 * MRU_LR;
        double dL_dwo23_t1 = dL_do3_t1 * d_sigmoid(o3_t1) * x2_t1 * MRU_LR;
        double dL_dwo33_t1 = dL_do3_t1 * d_sigmoid(o3_t1) * x3_t1 * MRU_LR;
        double dL_dwo43_t1 = dL_do3_t1 * d_sigmoid(o3_t1) * x4_t1 * MRU_LR;
        double dL_dwo53_t1 = dL_do3_t1 * d_sigmoid(o3_t1) * h1_t0 * MRU_LR;
        double dL_dwo63_t1 = dL_do3_t1 * d_sigmoid(o3_t1) * h2_t0 * MRU_LR;
        double dL_dwo73_t1 = dL_do3_t1 * d_sigmoid(o3_t1) * h3_t0 * MRU_LR;

        double dL_dwc11_t1 = dL_dc1_t1 * d_tanh(c1_t1) * x1_t1 * MRU_LR;
        double dL_dwc21_t1 = dL_dc1_t1 * d_tanh(c1_t1) * x2_t1 * MRU_LR;
        double dL_dwc31_t1 = dL_dc1_t1 * d_tanh(c1_t1) * x3_t1 * MRU_LR;
        double dL_dwc41_t1 = dL_dc1_t1 * d_tanh(c1_t1) * x4_t1 * MRU_LR;
        double dL_dwc51_t1 = dL_dc1_t1 * d_tanh(c1_t1) * h1_t0 * MRU_LR;
        double dL_dwc61_t1 = dL_dc1_t1 * d_tanh(c1_t1) * h2_t0 * MRU_LR;
        double dL_dwc71_t1 = dL_dc1_t1 * d_tanh(c1_t1) * h3_t0 * MRU_LR;

        double dL_dwc12_t1 = dL_dc2_t1 * d_tanh(c2_t1) * x1_t1 * MRU_LR;
        double dL_dwc22_t1 = dL_dc2_t1 * d_tanh(c2_t1) * x2_t1 * MRU_LR;
        double dL_dwc32_t1 = dL_dc2_t1 * d_tanh(c2_t1) * x3_t1 * MRU_LR;
        double dL_dwc42_t1 = dL_dc2_t1 * d_tanh(c2_t1) * x4_t1 * MRU_LR;
        double dL_dwc52_t1 = dL_dc2_t1 * d_tanh(c2_t1) * h1_t0 * MRU_LR;
        double dL_dwc62_t1 = dL_dc2_t1 * d_tanh(c2_t1) * h2_t0 * MRU_LR;
        double dL_dwc72_t1 = dL_dc2_t1 * d_tanh(c2_t1) * h3_t0 * MRU_LR;

        double dL_dwc13_t1 = dL_dc3_t1 * d_tanh(c3_t1) * x1_t1 * MRU_LR;
        double dL_dwc23_t1 = dL_dc3_t1 * d_tanh(c3_t1) * x2_t1 * MRU_LR;
        double dL_dwc33_t1 = dL_dc3_t1 * d_tanh(c3_t1) * x3_t1 * MRU_LR;
        double dL_dwc43_t1 = dL_dc3_t1 * d_tanh(c3_t1) * x4_t1 * MRU_LR;
        double dL_dwc53_t1 = dL_dc3_t1 * d_tanh(c3_t1) * h1_t0 * MRU_LR;
        double dL_dwc63_t1 = dL_dc3_t1 * d_tanh(c3_t1) * h2_t0 * MRU_LR;
        double dL_dwc73_t1 = dL_dc3_t1 * d_tanh(c3_t1) * h3_t0 * MRU_LR;

        double dL_dwi11_t1 = dL_di1_t1 * d_sigmoid(i1_t1) * x1_t1 * MRU_LR;
        double dL_dwi21_t1 = dL_di1_t1 * d_sigmoid(i1_t1) * x2_t1 * MRU_LR;
        double dL_dwi31_t1 = dL_di1_t1 * d_sigmoid(i1_t1) * x3_t1 * MRU_LR;
        double dL_dwi41_t1 = dL_di1_t1 * d_sigmoid(i1_t1) * x4_t1 * MRU_LR;
        double dL_dwi51_t1 = dL_di1_t1 * d_sigmoid(i1_t1) * h1_t0 * MRU_LR;
        double dL_dwi61_t1 = dL_di1_t1 * d_sigmoid(i1_t1) * h2_t0 * MRU_LR;
        double dL_dwi71_t1 = dL_di1_t1 * d_sigmoid(i1_t1) * h3_t0 * MRU_LR;

        double dL_dwi12_t1 = dL_di2_t1 * d_sigmoid(i2_t1) * x1_t1 * MRU_LR;
        double dL_dwi22_t1 = dL_di2_t1 * d_sigmoid(i2_t1) * x2_t1 * MRU_LR;
        double dL_dwi32_t1 = dL_di2_t1 * d_sigmoid(i2_t1) * x3_t1 * MRU_LR;
        double dL_dwi42_t1 = dL_di2_t1 * d_sigmoid(i2_t1) * x4_t1 * MRU_LR;
        double dL_dwi52_t1 = dL_di2_t1 * d_sigmoid(i2_t1) * h1_t0 * MRU_LR;
        double dL_dwi62_t1 = dL_di2_t1 * d_sigmoid(i2_t1) * h2_t0 * MRU_LR;
        double dL_dwi72_t1 = dL_di2_t1 * d_sigmoid(i2_t1) * h3_t0 * MRU_LR;

        double dL_dwi13_t1 = dL_di3_t1 * d_sigmoid(i3_t1) * x1_t1 * MRU_LR;
        double dL_dwi23_t1 = dL_di3_t1 * d_sigmoid(i3_t1) * x2_t1 * MRU_LR;
        double dL_dwi33_t1 = dL_di3_t1 * d_sigmoid(i3_t1) * x3_t1 * MRU_LR;
        double dL_dwi43_t1 = dL_di3_t1 * d_sigmoid(i3_t1) * x4_t1 * MRU_LR;
        double dL_dwi53_t1 = dL_di3_t1 * d_sigmoid(i3_t1) * h1_t0 * MRU_LR;
        double dL_dwi63_t1 = dL_di3_t1 * d_sigmoid(i3_t1) * h2_t0 * MRU_LR;
        double dL_dwi73_t1 = dL_di3_t1 * d_sigmoid(i3_t1) * h3_t0 * MRU_LR;

        double dL_dwf11_t1 = dL_df1_t1 * d_sigmoid(f1_t1) * x1_t1 * MRU_LR;
        double dL_dwf21_t1 = dL_df1_t1 * d_sigmoid(f1_t1) * x2_t1 * MRU_LR;
        double dL_dwf31_t1 = dL_df1_t1 * d_sigmoid(f1_t1) * x3_t1 * MRU_LR;
        double dL_dwf41_t1 = dL_df1_t1 * d_sigmoid(f1_t1) * x4_t1 * MRU_LR;
        double dL_dwf51_t1 = dL_df1_t1 * d_sigmoid(f1_t1) * h1_t0 * MRU_LR;
        double dL_dwf61_t1 = dL_df1_t1 * d_sigmoid(f1_t1) * h2_t0 * MRU_LR;
        double dL_dwf71_t1 = dL_df1_t1 * d_sigmoid(f1_t1) * h3_t0 * MRU_LR;

        double dL_dwf12_t1 = dL_df2_t1 * d_sigmoid(f2_t1) * x1_t1 * MRU_LR;
        double dL_dwf22_t1 = dL_df2_t1 * d_sigmoid(f2_t1) * x2_t1 * MRU_LR;
        double dL_dwf32_t1 = dL_df2_t1 * d_sigmoid(f2_t1) * x3_t1 * MRU_LR;
        double dL_dwf42_t1 = dL_df2_t1 * d_sigmoid(f2_t1) * x4_t1 * MRU_LR;
        double dL_dwf52_t1 = dL_df2_t1 * d_sigmoid(f2_t1) * h1_t0 * MRU_LR;
        double dL_dwf62_t1 = dL_df2_t1 * d_sigmoid(f2_t1) * h2_t0 * MRU_LR;
        double dL_dwf72_t1 = dL_df2_t1 * d_sigmoid(f2_t1) * h3_t0 * MRU_LR;

        double dL_dwf13_t1 = dL_df3_t1 * d_sigmoid(f3_t1) * x1_t1 * MRU_LR;
        double dL_dwf23_t1 = dL_df3_t1 * d_sigmoid(f3_t1) * x2_t1 * MRU_LR;
        double dL_dwf33_t1 = dL_df3_t1 * d_sigmoid(f3_t1) * x3_t1 * MRU_LR;
        double dL_dwf43_t1 = dL_df3_t1 * d_sigmoid(f3_t1) * x4_t1 * MRU_LR;
        double dL_dwf53_t1 = dL_df3_t1 * d_sigmoid(f3_t1) * h1_t0 * MRU_LR;
        double dL_dwf63_t1 = dL_df3_t1 * d_sigmoid(f3_t1) * h2_t0 * MRU_LR;
        double dL_dwf73_t1 = dL_df3_t1 * d_sigmoid(f3_t1) * h3_t0 * MRU_LR;

        // GENOME SET UP
        MRUConGene c11 = new MRUConGene(in1, out1), c21 = new MRUConGene(in2, out1), c31 = new MRUConGene(in3, out1), c41 = new MRUConGene(in4, out1);
        MRUConGene c51 = new MRUConGene(in5, out1), c61 = new MRUConGene(in6, out1), c71 = new MRUConGene(in7, out1);
        MRUConGene c12 = new MRUConGene(in1, out2), c22 = new MRUConGene(in2, out2), c32 = new MRUConGene(in3, out2), c42 = new MRUConGene(in4, out2);
        MRUConGene c52 = new MRUConGene(in5, out2), c62 = new MRUConGene(in6, out2), c72 = new MRUConGene(in7, out2);
        MRUConGene c13 = new MRUConGene(in1, out3), c23 = new MRUConGene(in2, out3), c33 = new MRUConGene(in3, out3), c43 = new MRUConGene(in4, out3);
        MRUConGene c53 = new MRUConGene(in5, out3), c63 = new MRUConGene(in6, out3), c73 = new MRUConGene(in7, out3);

        c11.setWeight(f_w11, 'f'); c21.setWeight(f_w21, 'f'); c31.setWeight(f_w31, 'f'); c41.setWeight(f_w41, 'f');
        c51.setWeight(f_w51, 'f'); c61.setWeight(f_w61, 'f'); c71.setWeight(f_w71, 'f');
        c12.setWeight(f_w12, 'f'); c22.setWeight(f_w22, 'f'); c32.setWeight(f_w32, 'f'); c42.setWeight(f_w42, 'f');
        c52.setWeight(f_w52, 'f'); c62.setWeight(f_w62, 'f'); c72.setWeight(f_w72, 'f');
        c13.setWeight(f_w13, 'f'); c23.setWeight(f_w23, 'f'); c33.setWeight(f_w33, 'f'); c43.setWeight(f_w43, 'f');
        c53.setWeight(f_w53, 'f'); c63.setWeight(f_w63, 'f'); c73.setWeight(f_w73, 'f');

        c11.setWeight(i_w11, 'i'); c21.setWeight(i_w21, 'i'); c31.setWeight(i_w31, 'i'); c41.setWeight(i_w41, 'i');
        c51.setWeight(i_w51, 'i'); c61.setWeight(i_w61, 'i'); c71.setWeight(i_w71, 'i');
        c12.setWeight(i_w12, 'i'); c22.setWeight(i_w22, 'i'); c32.setWeight(i_w32, 'i'); c42.setWeight(i_w42, 'i');
        c52.setWeight(i_w52, 'i'); c62.setWeight(i_w62, 'i'); c72.setWeight(i_w72, 'i');
        c13.setWeight(i_w13, 'i'); c23.setWeight(i_w23, 'i'); c33.setWeight(i_w33, 'i'); c43.setWeight(i_w43, 'i');
        c53.setWeight(i_w53, 'i'); c63.setWeight(i_w63, 'i'); c73.setWeight(i_w73, 'i');

        c11.setWeight(c_w11, 'c'); c21.setWeight(c_w21, 'c'); c31.setWeight(c_w31, 'c'); c41.setWeight(c_w41, 'c');
        c51.setWeight(c_w51, 'c'); c61.setWeight(c_w61, 'c'); c71.setWeight(c_w71, 'c');
        c12.setWeight(c_w12, 'c'); c22.setWeight(c_w22, 'c'); c32.setWeight(c_w32, 'c'); c42.setWeight(c_w42, 'c');
        c52.setWeight(c_w52, 'c'); c62.setWeight(c_w62, 'c'); c72.setWeight(c_w72, 'c');
        c13.setWeight(c_w13, 'c'); c23.setWeight(c_w23, 'c'); c33.setWeight(c_w33, 'c'); c43.setWeight(c_w43, 'c');
        c53.setWeight(c_w53, 'c'); c63.setWeight(c_w63, 'c'); c73.setWeight(c_w73, 'c');

        c11.setWeight(o_w11, 'o'); c21.setWeight(o_w21, 'o'); c31.setWeight(o_w31, 'o'); c41.setWeight(o_w41, 'o');
        c51.setWeight(o_w51, 'o'); c61.setWeight(o_w61, 'o'); c71.setWeight(o_w71, 'o');
        c12.setWeight(o_w12, 'o'); c22.setWeight(o_w22, 'o'); c32.setWeight(o_w32, 'o'); c42.setWeight(o_w42, 'o');
        c52.setWeight(o_w52, 'o'); c62.setWeight(o_w62, 'o'); c72.setWeight(o_w72, 'o');
        c13.setWeight(o_w13, 'o'); c23.setWeight(o_w23, 'o'); c33.setWeight(o_w33, 'o'); c43.setWeight(o_w43, 'o');
        c53.setWeight(o_w53, 'o'); c63.setWeight(o_w63, 'o'); c73.setWeight(o_w73, 'o');

        c11.setIN(1); c21.setIN(2); c31.setIN(3); c41.setIN(4); c51.setIN(5); c61.setIN(6); c71.setIN(7);
        c12.setIN(8); c22.setIN(9); c32.setIN(10); c42.setIN(11); c52.setIN(12); c62.setIN(13); c72.setIN(14);
        c13.setIN(15); c23.setIN(16); c33.setIN(17); c43.setIN(18); c53.setIN(19); c63.setIN(20); c73.setIN(21);

        out1.getInCons().addAll(Arrays.asList(c11, c21, c31, c41, c51, c61, c71));
        out2.getInCons().addAll(Arrays.asList(c12, c22, c32, c42, c52, c62, c72));
        out3.getInCons().addAll(Arrays.asList(c13, c23, c33, c43, c53, c63, c73));

        g.getCons().addAll(c11, c21, c31, c41, c51, c61, c71, c12, c22, c32, c42, c52, c62, c72, c13, c23, c33, c43, c53, c63, c73);

        g.express();

        // TEST FORWARD METHOD
        double[] y_pred = g.feed(inputSeq);

        assertArrayEquals(y, y_pred);

        // TEST BACK PROPAGATION METHOD
        g.train(inputSeq, actorGrads, criticGrads);

        assertDoubleApprox(o_w11 + dL_dwo11_t1 + dL_dwo11_t2, c11.getWeight('o'));
        assertDoubleApprox(o_w21 + dL_dwo21_t1 + dL_dwo21_t2, c21.getWeight('o'));
        assertDoubleApprox(o_w31 + dL_dwo31_t1 + dL_dwo31_t2, c31.getWeight('o'));
        assertDoubleApprox(o_w41 + dL_dwo41_t1 + dL_dwo41_t2, c41.getWeight('o'));
        assertDoubleApprox(o_w51 + dL_dwo51_t1 + dL_dwo51_t2, c51.getWeight('o'));
        assertDoubleApprox(o_w61 + dL_dwo61_t1 + dL_dwo61_t2, c61.getWeight('o'));
        assertDoubleApprox(o_w71 + dL_dwo71_t1 + dL_dwo71_t2, c71.getWeight('o'));

        assertDoubleApprox(o_w12 + dL_dwo12_t1 + dL_dwo12_t2, c12.getWeight('o'));
        assertDoubleApprox(o_w22 + dL_dwo22_t1 + dL_dwo22_t2, c22.getWeight('o'));
        assertDoubleApprox(o_w32 + dL_dwo32_t1 + dL_dwo32_t2, c32.getWeight('o'));
        assertDoubleApprox(o_w42 + dL_dwo42_t1 + dL_dwo42_t2, c42.getWeight('o'));
        assertDoubleApprox(o_w52 + dL_dwo52_t1 + dL_dwo52_t2, c52.getWeight('o'));
        assertDoubleApprox(o_w62 + dL_dwo62_t1 + dL_dwo62_t2, c62.getWeight('o'));
        assertDoubleApprox(o_w72 + dL_dwo72_t1 + dL_dwo72_t2, c72.getWeight('o'));

        assertDoubleApprox(o_w13 + dL_dwo13_t1 + dL_dwo13_t2, c13.getWeight('o'));
        assertDoubleApprox(o_w23 + dL_dwo23_t1 + dL_dwo23_t2, c23.getWeight('o'));
        assertDoubleApprox(o_w33 + dL_dwo33_t1 + dL_dwo33_t2, c33.getWeight('o'));
        assertDoubleApprox(o_w43 + dL_dwo43_t1 + dL_dwo43_t2, c43.getWeight('o'));
        assertDoubleApprox(o_w53 + dL_dwo53_t1 + dL_dwo53_t2, c53.getWeight('o'));
        assertDoubleApprox(o_w63 + dL_dwo63_t1 + dL_dwo63_t2, c63.getWeight('o'));
        assertDoubleApprox(o_w73 + dL_dwo73_t1 + dL_dwo73_t2, c73.getWeight('o'));

        assertDoubleApprox(c_w11 + dL_dwc11_t1 + dL_dwc11_t2, c11.getWeight('c'));
        assertDoubleApprox(c_w21 + dL_dwc21_t1 + dL_dwc21_t2, c21.getWeight('c'));
        assertDoubleApprox(c_w31 + dL_dwc31_t1 + dL_dwc31_t2, c31.getWeight('c'));
        assertDoubleApprox(c_w41 + dL_dwc41_t1 + dL_dwc41_t2, c41.getWeight('c'));
        assertDoubleApprox(c_w51 + dL_dwc51_t1 + dL_dwc51_t2, c51.getWeight('c'));
        assertDoubleApprox(c_w61 + dL_dwc61_t1 + dL_dwc61_t2, c61.getWeight('c'));
        assertDoubleApprox(c_w71 + dL_dwc71_t1 + dL_dwc71_t2, c71.getWeight('c'));

        assertDoubleApprox(c_w12 + dL_dwc12_t1 + dL_dwc12_t2, c12.getWeight('c'));
        assertDoubleApprox(c_w22 + dL_dwc22_t1 + dL_dwc22_t2, c22.getWeight('c'));
        assertDoubleApprox(c_w32 + dL_dwc32_t1 + dL_dwc32_t2, c32.getWeight('c'));
        assertDoubleApprox(c_w42 + dL_dwc42_t1 + dL_dwc42_t2, c42.getWeight('c'));
        assertDoubleApprox(c_w52 + dL_dwc52_t1 + dL_dwc52_t2, c52.getWeight('c'));
        assertDoubleApprox(c_w62 + dL_dwc62_t1 + dL_dwc62_t2, c62.getWeight('c'));
        assertDoubleApprox(c_w72 + dL_dwc72_t1 + dL_dwc72_t2, c72.getWeight('c'));

        assertDoubleApprox(c_w13 + dL_dwc13_t1 + dL_dwc13_t2, c13.getWeight('c'));
        assertDoubleApprox(c_w23 + dL_dwc23_t1 + dL_dwc23_t2, c23.getWeight('c'));
        assertDoubleApprox(c_w33 + dL_dwc33_t1 + dL_dwc33_t2, c33.getWeight('c'));
        assertDoubleApprox(c_w43 + dL_dwc43_t1 + dL_dwc43_t2, c43.getWeight('c'));
        assertDoubleApprox(c_w53 + dL_dwc53_t1 + dL_dwc53_t2, c53.getWeight('c'));
        assertDoubleApprox(c_w63 + dL_dwc63_t1 + dL_dwc63_t2, c63.getWeight('c'));
        assertDoubleApprox(c_w73 + dL_dwc73_t1 + dL_dwc73_t2, c73.getWeight('c'));

        assertDoubleApprox(i_w11 + dL_dwi11_t1 + dL_dwi11_t2, c11.getWeight('i'));
        assertDoubleApprox(i_w21 + dL_dwi21_t1 + dL_dwi21_t2, c21.getWeight('i'));
        assertDoubleApprox(i_w31 + dL_dwi31_t1 + dL_dwi31_t2, c31.getWeight('i'));
        assertDoubleApprox(i_w41 + dL_dwi41_t1 + dL_dwi41_t2, c41.getWeight('i'));
        assertDoubleApprox(i_w51 + dL_dwi51_t1 + dL_dwi51_t2, c51.getWeight('i'));
        assertDoubleApprox(i_w61 + dL_dwi61_t1 + dL_dwi61_t2, c61.getWeight('i'));
        assertDoubleApprox(i_w71 + dL_dwi71_t1 + dL_dwi71_t2, c71.getWeight('i'));

        assertDoubleApprox(i_w12 + dL_dwi12_t1 + dL_dwi12_t2, c12.getWeight('i'));
        assertDoubleApprox(i_w22 + dL_dwi22_t1 + dL_dwi22_t2, c22.getWeight('i'));
        assertDoubleApprox(i_w32 + dL_dwi32_t1 + dL_dwi32_t2, c32.getWeight('i'));
        assertDoubleApprox(i_w42 + dL_dwi42_t1 + dL_dwi42_t2, c42.getWeight('i'));
        assertDoubleApprox(i_w52 + dL_dwi52_t1 + dL_dwi52_t2, c52.getWeight('i'));
        assertDoubleApprox(i_w62 + dL_dwi62_t1 + dL_dwi62_t2, c62.getWeight('i'));
        assertDoubleApprox(i_w72 + dL_dwi72_t1 + dL_dwi72_t2, c72.getWeight('i'));

        assertDoubleApprox(i_w13 + dL_dwi13_t1 + dL_dwi13_t2, c13.getWeight('i'));
        assertDoubleApprox(i_w23 + dL_dwi23_t1 + dL_dwi23_t2, c23.getWeight('i'));
        assertDoubleApprox(i_w33 + dL_dwi33_t1 + dL_dwi33_t2, c33.getWeight('i'));
        assertDoubleApprox(i_w43 + dL_dwi43_t1 + dL_dwi43_t2, c43.getWeight('i'));
        assertDoubleApprox(i_w53 + dL_dwi53_t1 + dL_dwi53_t2, c53.getWeight('i'));
        assertDoubleApprox(i_w63 + dL_dwi63_t1 + dL_dwi63_t2, c63.getWeight('i'));
        assertDoubleApprox(i_w73 + dL_dwi73_t1 + dL_dwi73_t2, c73.getWeight('i'));

        assertDoubleApprox(f_w11 + dL_dwf11_t1 + dL_dwf11_t2, c11.getWeight('f'));
        assertDoubleApprox(f_w21 + dL_dwf21_t1 + dL_dwf21_t2, c21.getWeight('f'));
        assertDoubleApprox(f_w31 + dL_dwf31_t1 + dL_dwf31_t2, c31.getWeight('f'));
        assertDoubleApprox(f_w41 + dL_dwf41_t1 + dL_dwf41_t2, c41.getWeight('f'));
        assertDoubleApprox(f_w51 + dL_dwf51_t1 + dL_dwf51_t2, c51.getWeight('f'));
        assertDoubleApprox(f_w61 + dL_dwf61_t1 + dL_dwf61_t2, c61.getWeight('f'));
        assertDoubleApprox(f_w71 + dL_dwf71_t1 + dL_dwf71_t2, c71.getWeight('f'));

        assertDoubleApprox(f_w12 + dL_dwf12_t1 + dL_dwf12_t2, c12.getWeight('f'));
        assertDoubleApprox(f_w22 + dL_dwf22_t1 + dL_dwf22_t2, c22.getWeight('f'));
        assertDoubleApprox(f_w32 + dL_dwf32_t1 + dL_dwf32_t2, c32.getWeight('f'));
        assertDoubleApprox(f_w42 + dL_dwf42_t1 + dL_dwf42_t2, c42.getWeight('f'));
        assertDoubleApprox(f_w52 + dL_dwf52_t1 + dL_dwf52_t2, c52.getWeight('f'));
        assertDoubleApprox(f_w62 + dL_dwf62_t1 + dL_dwf62_t2, c62.getWeight('f'));
        assertDoubleApprox(f_w72 + dL_dwf72_t1 + dL_dwf72_t2, c72.getWeight('f'));

        assertDoubleApprox(f_w13 + dL_dwf13_t1 + dL_dwf13_t2, c13.getWeight('f'));
        assertDoubleApprox(f_w23 + dL_dwf23_t1 + dL_dwf23_t2, c23.getWeight('f'));
        assertDoubleApprox(f_w33 + dL_dwf33_t1 + dL_dwf33_t2, c33.getWeight('f'));
        assertDoubleApprox(f_w43 + dL_dwf43_t1 + dL_dwf43_t2, c43.getWeight('f'));
        assertDoubleApprox(f_w53 + dL_dwf53_t1 + dL_dwf53_t2, c53.getWeight('f'));
        assertDoubleApprox(f_w63 + dL_dwf63_t1 + dL_dwf63_t2, c63.getWeight('f'));
        assertDoubleApprox(f_w73 + dL_dwf73_t1 + dL_dwf73_t2, c73.getWeight('f'));

    }

    @RepeatedTest(100)
    public void testDeepDense() {

        // VALUE SET UP
        double x1_t1 = r(), x2_t1 = r(), x3_t1 = r(), x4_t1 = r();
        double x1_t2 = r(), x2_t2 = r(), x3_t2 = r(), x4_t2 = r();
        double f_w11h = r(), f_w21h = r(), f_w31h = r(), f_w41h = r(), f_w51h = r(), f_w61h = r(), f_w71h = r();
        double f_w12h = r(), f_w22h = r(), f_w32h = r(), f_w42h = r(), f_w52h = r(), f_w62h = r(), f_w72h = r();
        double i_w11h = r(), i_w21h = r(), i_w31h = r(), i_w41h = r(), i_w51h = r(), i_w61h = r(), i_w71h = r();
        double i_w12h = r(), i_w22h = r(), i_w32h = r(), i_w42h = r(), i_w52h = r(), i_w62h = r(), i_w72h = r();
        double c_w11h = r(), c_w21h = r(), c_w31h = r(), c_w41h = r(), c_w51h = r(), c_w61h = r(), c_w71h = r();
        double c_w12h = r(), c_w22h = r(), c_w32h = r(), c_w42h = r(), c_w52h = r(), c_w62h = r(), c_w72h = r();
        double o_w11h = r(), o_w21h = r(), o_w31h = r(), o_w41h = r(), o_w51h = r(), o_w61h = r(), o_w71h = r();
        double o_w12h = r(), o_w22h = r(), o_w32h = r(), o_w42h = r(), o_w52h = r(), o_w62h = r(), o_w72h = r();
        double f_w1h1 = r(), f_w2h1 = r();
        double f_w1h2 = r(), f_w2h2 = r();
        double f_w1h3 = r(), f_w2h3 = r();
        double i_w1h1 = r(), i_w2h1 = r();
        double i_w1h2 = r(), i_w2h2 = r();
        double i_w1h3 = r(), i_w2h3 = r();
        double c_w1h1 = r(), c_w2h1 = r();
        double c_w1h2 = r(), c_w2h2 = r();
        double c_w1h3 = r(), c_w2h3 = r();
        double o_w1h1 = r(), o_w2h1 = r();
        double o_w1h2 = r(), o_w2h2 = r();
        double o_w1h3 = r(), o_w2h3 = r();

        ArrayList<double[]> inputSeq = new ArrayList<>(Arrays.asList(
                new double[]{x1_t1, x2_t1, x3_t1, x4_t1},
                new double[]{x1_t2, x2_t2, x3_t2, x4_t2}
        ));

        // TRUE VALUE CALCULATION
        // FORWARDING
        double C1_t0 = 0, C2_t0 = 0, C3_t0 = 0;
        double h1_t0 = 0, h2_t0 = 0, h3_t0 = 0;
        // forget gate at time step 1
        double fh1_t1 = sigmoid(x1_t1 * f_w11h + x2_t1 * f_w21h + x3_t1 * f_w31h + x4_t1 * f_w41h + h1_t0 * f_w51h + h2_t0 * f_w61h + h3_t0 * f_w71h);
        double fh2_t1 = sigmoid(x1_t1 * f_w12h + x2_t1 * f_w22h + x3_t1 * f_w32h + x4_t1 * f_w42h + h1_t0 * f_w52h + h2_t0 * f_w62h + h3_t0 * f_w72h);
        double f1_t1 = sigmoid(fh1_t1 * f_w1h1 + fh2_t1 * f_w2h1);
        double f2_t1 = sigmoid(fh1_t1 * f_w1h2 + fh2_t1 * f_w2h2);
        double f3_t1 = sigmoid(fh1_t1 * f_w1h3 + fh2_t1 * f_w2h3);
        // input gate at time step 1
        double ih1_t1 = sigmoid(x1_t1 * i_w11h + x2_t1 * i_w21h + x3_t1 * i_w31h + x4_t1 * i_w41h + h1_t0 * i_w51h + h2_t0 * i_w61h + h3_t0 * i_w71h);
        double ih2_t1 = sigmoid(x1_t1 * i_w12h + x2_t1 * i_w22h + x3_t1 * i_w32h + x4_t1 * i_w42h + h1_t0 * i_w52h + h2_t0 * i_w62h + h3_t0 * i_w72h);
        double i1_t1 = sigmoid(ih1_t1 * i_w1h1 + ih2_t1 * i_w2h1);
        double i2_t1 = sigmoid(ih1_t1 * i_w1h2 + ih2_t1 * i_w2h2);
        double i3_t1 = sigmoid(ih1_t1 * i_w1h3 + ih2_t1 * i_w2h3);
        // candidate gate at time step 1
        double ch1_t1 = tanh(x1_t1 * c_w11h + x2_t1 * c_w21h + x3_t1 * c_w31h + x4_t1 * c_w41h + h1_t0 * c_w51h + h2_t0 * c_w61h + h3_t0 * c_w71h);
        double ch2_t1 = tanh(x1_t1 * c_w12h + x2_t1 * c_w22h + x3_t1 * c_w32h + x4_t1 * c_w42h + h1_t0 * c_w52h + h2_t0 * c_w62h + h3_t0 * c_w72h);
        double c1_t1 = tanh(ch1_t1 * c_w1h1 + ch2_t1 * c_w2h1);
        double c2_t1 = tanh(ch1_t1 * c_w1h2 + ch2_t1 * c_w2h2);
        double c3_t1 = tanh(ch1_t1 * c_w1h3 + ch2_t1 * c_w2h3);
        // output gate at time step 1
        double oh1_t1 = sigmoid(x1_t1 * o_w11h + x2_t1 * o_w21h + x3_t1 * o_w31h + x4_t1 * o_w41h + h1_t0 * o_w51h + h2_t0 * o_w61h + h3_t0 * o_w71h);
        double oh2_t1 = sigmoid(x1_t1 * o_w12h + x2_t1 * o_w22h + x3_t1 * o_w32h + x4_t1 * o_w42h + h1_t0 * o_w52h + h2_t0 * o_w62h + h3_t0 * o_w72h);
        double o1_t1 = sigmoid(oh1_t1 * o_w1h1 + oh2_t1 * o_w2h1);
        double o2_t1 = sigmoid(oh1_t1 * o_w1h2 + oh2_t1 * o_w2h2);
        double o3_t1 = sigmoid(oh1_t1 * o_w1h3 + oh2_t1 * o_w2h3);
        // cell state at time step 1
        double C1_t1 = f1_t1 * C1_t0 + i1_t1 * c1_t1;
        double C2_t1 = f2_t1 * C2_t0 + i2_t1 * c2_t1;
        double C3_t1 = f3_t1 * C3_t0 + i3_t1 * c3_t1;
        // hidden state at time step 1
        double h1_t1 = o1_t1 * tanh(C1_t1);
        double h2_t1 = o2_t1 * tanh(C2_t1);
        double h3_t1 = o3_t1 * tanh(C3_t1);
        // forget gate at time step 2
        double fh1_t2 = sigmoid(x1_t2 * f_w11h + x2_t2 * f_w21h + x3_t2 * f_w31h + x4_t2 * f_w41h + h1_t1 * f_w51h + h2_t1 * f_w61h + h3_t1 * f_w71h);
        double fh2_t2 = sigmoid(x1_t2 * f_w12h + x2_t2 * f_w22h + x3_t2 * f_w32h + x4_t2 * f_w42h + h1_t1 * f_w52h + h2_t1 * f_w62h + h3_t1 * f_w72h);
        double f1_t2 = sigmoid(fh1_t2 * f_w1h1 + fh2_t2 * f_w2h1);
        double f2_t2 = sigmoid(fh1_t2 * f_w1h2 + fh2_t2 * f_w2h2);
        double f3_t2 = sigmoid(fh1_t2 * f_w1h3 + fh2_t2 * f_w2h3);
        // input gate at time step 2
        double ih1_t2 = sigmoid(x1_t2 * i_w11h + x2_t2 * i_w21h + x3_t2 * i_w31h + x4_t2 * i_w41h + h1_t1 * i_w51h + h2_t1 * i_w61h + h3_t1 * i_w71h);
        double ih2_t2 = sigmoid(x1_t2 * i_w12h + x2_t2 * i_w22h + x3_t2 * i_w32h + x4_t2 * i_w42h + h1_t1 * i_w52h + h2_t1 * i_w62h + h3_t1 * i_w72h);
        double i1_t2 = sigmoid(ih1_t2 * i_w1h1 + ih2_t2 * i_w2h1);
        double i2_t2 = sigmoid(ih1_t2 * i_w1h2 + ih2_t2 * i_w2h2);
        double i3_t2 = sigmoid(ih1_t2 * i_w1h3 + ih2_t2 * i_w2h3);
        // candidate gate at time step 2
        double ch1_t2 = tanh(x1_t2 * c_w11h + x2_t2 * c_w21h + x3_t2 * c_w31h + x4_t2 * c_w41h + h1_t1 * c_w51h + h2_t1 * c_w61h + h3_t1 * c_w71h);
        double ch2_t2 = tanh(x1_t2 * c_w12h + x2_t2 * c_w22h + x3_t2 * c_w32h + x4_t2 * c_w42h + h1_t1 * c_w52h + h2_t1 * c_w62h + h3_t1 * c_w72h);
        double c1_t2 = tanh(ch1_t2 * c_w1h1 + ch2_t2 * c_w2h1);
        double c2_t2 = tanh(ch1_t2 * c_w1h2 + ch2_t2 * c_w2h2);
        double c3_t2 = tanh(ch1_t2 * c_w1h3 + ch2_t2 * c_w2h3);
        // output gate at time step 2
        double oh1_t2 = sigmoid(x1_t2 * o_w11h + x2_t2 * o_w21h + x3_t2 * o_w31h + x4_t2 * o_w41h + h1_t1 * o_w51h + h2_t1 * o_w61h + h3_t1 * o_w71h);
        double oh2_t2 = sigmoid(x1_t2 * o_w12h + x2_t2 * o_w22h + x3_t2 * o_w32h + x4_t2 * o_w42h + h1_t1 * o_w52h + h2_t1 * o_w62h + h3_t1 * o_w72h);
        double o1_t2 = sigmoid(oh1_t2 * o_w1h1 + oh2_t2 * o_w2h1);
        double o2_t2 = sigmoid(oh1_t2 * o_w1h2 + oh2_t2 * o_w2h2);
        double o3_t2 = sigmoid(oh1_t2 * o_w1h3 + oh2_t2 * o_w2h3);
        // cell state at time step 2
        double C1_t2 = f1_t2 * C1_t1 + i1_t2 * c1_t2;
        double C2_t2 = f2_t2 * C2_t1 + i2_t2 * c2_t2;
        double C3_t2 = f3_t2 * C3_t1 + i3_t2 * c3_t2;
        // hidden state at time step 2
        double h1_t2 = o1_t2 * tanh(C1_t2);
        double h2_t2 = o2_t2 * tanh(C2_t2);
        double h3_t2 = o3_t2 * tanh(C3_t2);

        double[] y = new double[]{h1_t2, h2_t2, h3_t2};

        // BACK PROPAGATING
        double a_dL_dh1_t2 = r(), a_dL_dh2_t2 = r(), a_dL_dh3_t2 = r(); // assume actor gradient
        double c_dL_dh1_t2 = r(), c_dL_dh2_t2 = r(), c_dL_dh3_t2 = r(); // assume critic gradient
        HashMap<Integer, Double> actorGrads  = new HashMap<>(){{ put(1, a_dL_dh1_t2); put(2, a_dL_dh2_t2); put(3, a_dL_dh3_t2); }};
        HashMap<Integer, Double> criticGrads  = new HashMap<>(){{ put(1, c_dL_dh1_t2); put(2, c_dL_dh2_t2); put(3, c_dL_dh3_t2); }};

        double dL_dh1_t2 = a_dL_dh1_t2 + c_dL_dh1_t2;
        double dL_dh2_t2 = a_dL_dh2_t2 + c_dL_dh2_t2;
        double dL_dh3_t2 = a_dL_dh3_t2 + c_dL_dh3_t2;

        // at time 2
        double dL_dC1_t2 = dL_dh1_t2 * o1_t2 * (1 - Math.pow(tanh(C1_t2), 2));
        double dL_dC2_t2 = dL_dh2_t2 * o2_t2 * (1 - Math.pow(tanh(C2_t2), 2));
        double dL_dC3_t2 = dL_dh3_t2 * o3_t2 * (1 - Math.pow(tanh(C3_t2), 2));

        double dL_do1_t2 = dL_dh1_t2 * tanh(C1_t2), dL_do2_t2 = dL_dh2_t2 * tanh(C2_t2), dL_do3_t2 = dL_dh3_t2 * tanh(C3_t2);
        double dL_df1_t2 = dL_dC1_t2 * C1_t1, dL_df2_t2 = dL_dC2_t2 * C2_t1, dL_df3_t2 = dL_dC3_t2 * C3_t1;
        double dL_di1_t2 = dL_dC1_t2 * c1_t2, dL_di2_t2 = dL_dC2_t2 * c2_t2, dL_di3_t2 = dL_dC3_t2 * c3_t2;
        double dL_dc1_t2 = dL_dC1_t2 * i1_t2, dL_dc2_t2 = dL_dC2_t2 * i2_t2, dL_dc3_t2 = dL_dC3_t2 * i3_t2;

        double dL_doz1_t2 = dL_do1_t2 * d_sigmoid(o1_t2), dL_doz2_t2 = dL_do2_t2 * d_sigmoid(o2_t2), dL_doz3_t2 = dL_do3_t2 * d_sigmoid(o3_t2);
        double dL_dwo1h1_t2 = dL_doz1_t2 * oh1_t2 * MRU_LR;
        double dL_dwo1h2_t2 = dL_doz2_t2 * oh1_t2 * MRU_LR;
        double dL_dwo1h3_t2 = dL_doz3_t2 * oh1_t2 * MRU_LR;
        double dL_dwo2h1_t2 = dL_doz1_t2 * oh2_t2 * MRU_LR;
        double dL_dwo2h2_t2 = dL_doz2_t2 * oh2_t2 * MRU_LR;
        double dL_dwo2h3_t2 = dL_doz3_t2 * oh2_t2 * MRU_LR;

        double o_dL_dyz1_t2 = dL_doz1_t2 * o_w1h1 + dL_doz2_t2 * o_w1h2 + dL_doz3_t2 * o_w1h3;
        double o_dL_dzz1_t2 = o_dL_dyz1_t2 * d_sigmoid(oh1_t2);
        double dL_dwo11h_t2 = o_dL_dzz1_t2 * x1_t2 * MRU_LR;
        double dL_dwo21h_t2 = o_dL_dzz1_t2 * x2_t2 * MRU_LR;
        double dL_dwo31h_t2 = o_dL_dzz1_t2 * x3_t2 * MRU_LR;
        double dL_dwo41h_t2 = o_dL_dzz1_t2 * x4_t2 * MRU_LR;
        double dL_dwo51h_t2 = o_dL_dzz1_t2 * h1_t1 * MRU_LR;
        double dL_dwo61h_t2 = o_dL_dzz1_t2 * h2_t1 * MRU_LR;
        double dL_dwo71h_t2 = o_dL_dzz1_t2 * h3_t1 * MRU_LR;

        double o_dL_dyz2_t2 = dL_doz1_t2 * o_w2h1 + dL_doz2_t2 * o_w2h2 + dL_doz3_t2 * o_w2h3;
        double o_dL_dzz2_t2 = o_dL_dyz2_t2 * d_sigmoid(oh2_t2);
        double dL_dwo12h_t2 = o_dL_dzz2_t2 * x1_t2 * MRU_LR;
        double dL_dwo22h_t2 = o_dL_dzz2_t2 * x2_t2 * MRU_LR;
        double dL_dwo32h_t2 = o_dL_dzz2_t2 * x3_t2 * MRU_LR;
        double dL_dwo42h_t2 = o_dL_dzz2_t2 * x4_t2 * MRU_LR;
        double dL_dwo52h_t2 = o_dL_dzz2_t2 * h1_t1 * MRU_LR;
        double dL_dwo62h_t2 = o_dL_dzz2_t2 * h2_t1 * MRU_LR;
        double dL_dwo72h_t2 = o_dL_dzz2_t2 * h3_t1 * MRU_LR;

        double dL_dcz1_t2 = dL_dc1_t2 * d_tanh(c1_t2), dL_dcz2_t2 = dL_dc2_t2 * d_tanh(c2_t2), dL_dcz3_t2 = dL_dc3_t2 * d_tanh(c3_t2);
        double dL_dwc1h1_t2 = dL_dcz1_t2 * ch1_t2 * MRU_LR;
        double dL_dwc1h2_t2 = dL_dcz2_t2 * ch1_t2 * MRU_LR;
        double dL_dwc1h3_t2 = dL_dcz3_t2 * ch1_t2 * MRU_LR;
        double dL_dwc2h1_t2 = dL_dcz1_t2 * ch2_t2 * MRU_LR;
        double dL_dwc2h2_t2 = dL_dcz2_t2 * ch2_t2 * MRU_LR;
        double dL_dwc2h3_t2 = dL_dcz3_t2 * ch2_t2 * MRU_LR;

        double c_dL_dyz1_t2 = dL_dcz1_t2 * c_w1h1 + dL_dcz2_t2 * c_w1h2 + dL_dcz3_t2 * c_w1h3;
        double c_dL_dzz1_t2 = c_dL_dyz1_t2 * d_tanh(ch1_t2);
        double dL_dwc11h_t2 = c_dL_dzz1_t2 * x1_t2 * MRU_LR;
        double dL_dwc21h_t2 = c_dL_dzz1_t2 * x2_t2 * MRU_LR;
        double dL_dwc31h_t2 = c_dL_dzz1_t2 * x3_t2 * MRU_LR;
        double dL_dwc41h_t2 = c_dL_dzz1_t2 * x4_t2 * MRU_LR;
        double dL_dwc51h_t2 = c_dL_dzz1_t2 * h1_t1 * MRU_LR;
        double dL_dwc61h_t2 = c_dL_dzz1_t2 * h2_t1 * MRU_LR;
        double dL_dwc71h_t2 = c_dL_dzz1_t2 * h3_t1 * MRU_LR;

        double c_dL_dyz2_t2 = dL_dcz1_t2 * c_w2h1 + dL_dcz2_t2 * c_w2h2 + dL_dcz3_t2 * c_w2h3;
        double c_dL_dzz2_t2 = c_dL_dyz2_t2 * d_tanh(ch2_t2);
        double dL_dwc12h_t2 = c_dL_dzz2_t2 * x1_t2 * MRU_LR;
        double dL_dwc22h_t2 = c_dL_dzz2_t2 * x2_t2 * MRU_LR;
        double dL_dwc32h_t2 = c_dL_dzz2_t2 * x3_t2 * MRU_LR;
        double dL_dwc42h_t2 = c_dL_dzz2_t2 * x4_t2 * MRU_LR;
        double dL_dwc52h_t2 = c_dL_dzz2_t2 * h1_t1 * MRU_LR;
        double dL_dwc62h_t2 = c_dL_dzz2_t2 * h2_t1 * MRU_LR;
        double dL_dwc72h_t2 = c_dL_dzz2_t2 * h3_t1 * MRU_LR;

        double dL_diz1_t2 = dL_di1_t2 * d_sigmoid(i1_t2), dL_diz2_t2 = dL_di2_t2 * d_sigmoid(i2_t2), dL_diz3_t2 = dL_di3_t2 * d_sigmoid(i3_t2);
        double dL_dwi1h1_t2 = dL_diz1_t2 * ih1_t2 * MRU_LR;
        double dL_dwi1h2_t2 = dL_diz2_t2 * ih1_t2 * MRU_LR;
        double dL_dwi1h3_t2 = dL_diz3_t2 * ih1_t2 * MRU_LR;
        double dL_dwi2h1_t2 = dL_diz1_t2 * ih2_t2 * MRU_LR;
        double dL_dwi2h2_t2 = dL_diz2_t2 * ih2_t2 * MRU_LR;
        double dL_dwi2h3_t2 = dL_diz3_t2 * ih2_t2 * MRU_LR;

        double i_dL_dyz1_t2 = dL_diz1_t2 * i_w1h1 + dL_diz2_t2 * i_w1h2 + dL_diz3_t2 * i_w1h3;
        double i_dL_dzz1_t2 = i_dL_dyz1_t2 * d_sigmoid(ih1_t2);
        double dL_dwi11h_t2 = i_dL_dzz1_t2 * x1_t2 * MRU_LR;
        double dL_dwi21h_t2 = i_dL_dzz1_t2 * x2_t2 * MRU_LR;
        double dL_dwi31h_t2 = i_dL_dzz1_t2 * x3_t2 * MRU_LR;
        double dL_dwi41h_t2 = i_dL_dzz1_t2 * x4_t2 * MRU_LR;
        double dL_dwi51h_t2 = i_dL_dzz1_t2 * h1_t1 * MRU_LR;
        double dL_dwi61h_t2 = i_dL_dzz1_t2 * h2_t1 * MRU_LR;
        double dL_dwi71h_t2 = i_dL_dzz1_t2 * h3_t1 * MRU_LR;

        double i_dL_dyz2_t2 = dL_diz1_t2 * i_w2h1 + dL_diz2_t2 * i_w2h2 + dL_diz3_t2 * i_w2h3;
        double i_dL_dzz2_t2 = i_dL_dyz2_t2 * d_sigmoid(ih2_t2);
        double dL_dwi12h_t2 = i_dL_dzz2_t2 * x1_t2 * MRU_LR;
        double dL_dwi22h_t2 = i_dL_dzz2_t2 * x2_t2 * MRU_LR;
        double dL_dwi32h_t2 = i_dL_dzz2_t2 * x3_t2 * MRU_LR;
        double dL_dwi42h_t2 = i_dL_dzz2_t2 * x4_t2 * MRU_LR;
        double dL_dwi52h_t2 = i_dL_dzz2_t2 * h1_t1 * MRU_LR;
        double dL_dwi62h_t2 = i_dL_dzz2_t2 * h2_t1 * MRU_LR;
        double dL_dwi72h_t2 = i_dL_dzz2_t2 * h3_t1 * MRU_LR;

        double dL_dfz1_t2 = dL_df1_t2 * d_sigmoid(f1_t2), dL_dfz2_t2 = dL_df2_t2 * d_sigmoid(f2_t2), dL_dfz3_t2 = dL_df3_t2 * d_sigmoid(f3_t2);
        double dL_dwf1h1_t2 = dL_dfz1_t2 * fh1_t2 * MRU_LR;
        double dL_dwf1h2_t2 = dL_dfz2_t2 * fh1_t2 * MRU_LR;
        double dL_dwf1h3_t2 = dL_dfz3_t2 * fh1_t2 * MRU_LR;
        double dL_dwf2h1_t2 = dL_dfz1_t2 * fh2_t2 * MRU_LR;
        double dL_dwf2h2_t2 = dL_dfz2_t2 * fh2_t2 * MRU_LR;
        double dL_dwf2h3_t2 = dL_dfz3_t2 * fh2_t2 * MRU_LR;

        double f_dL_dyz1_t2 = dL_dfz1_t2 * f_w1h1 + dL_dfz2_t2 * f_w1h2 + dL_dfz3_t2 * f_w1h3;
        double f_dL_dzz1_t2 = f_dL_dyz1_t2 * d_sigmoid(fh1_t2);
        double dL_dwf11h_t2 = f_dL_dzz1_t2 * x1_t2 * MRU_LR;
        double dL_dwf21h_t2 = f_dL_dzz1_t2 * x2_t2 * MRU_LR;
        double dL_dwf31h_t2 = f_dL_dzz1_t2 * x3_t2 * MRU_LR;
        double dL_dwf41h_t2 = f_dL_dzz1_t2 * x4_t2 * MRU_LR;
        double dL_dwf51h_t2 = f_dL_dzz1_t2 * h1_t1 * MRU_LR;
        double dL_dwf61h_t2 = f_dL_dzz1_t2 * h2_t1 * MRU_LR;
        double dL_dwf71h_t2 = f_dL_dzz1_t2 * h3_t1 * MRU_LR;

        double f_dL_dyz2_t2 = dL_dfz1_t2 * f_w2h1 + dL_dfz2_t2 * f_w2h2 + dL_dfz3_t2 * f_w2h3;
        double f_dL_dzz2_t2 = f_dL_dyz2_t2 * d_sigmoid(fh2_t2);
        double dL_dwf12h_t2 = f_dL_dzz2_t2 * x1_t2 * MRU_LR;
        double dL_dwf22h_t2 = f_dL_dzz2_t2 * x2_t2 * MRU_LR;
        double dL_dwf32h_t2 = f_dL_dzz2_t2 * x3_t2 * MRU_LR;
        double dL_dwf42h_t2 = f_dL_dzz2_t2 * x4_t2 * MRU_LR;
        double dL_dwf52h_t2 = f_dL_dzz2_t2 * h1_t1 * MRU_LR;
        double dL_dwf62h_t2 = f_dL_dzz2_t2 * h2_t1 * MRU_LR;
        double dL_dwf72h_t2 = f_dL_dzz2_t2 * h3_t1 * MRU_LR;

        double dL_dh1_t1 =
                o_dL_dzz1_t2 * o_w51h + o_dL_dzz2_t2 * o_w52h +
                c_dL_dzz1_t2 * c_w51h + c_dL_dzz2_t2 * c_w52h +
                i_dL_dzz1_t2 * i_w51h + i_dL_dzz2_t2 * i_w52h +
                f_dL_dzz1_t2 * f_w51h + f_dL_dzz2_t2 * f_w52h;
        double dL_dh2_t1 =
                o_dL_dzz1_t2 * o_w61h + o_dL_dzz2_t2 * o_w62h +
                c_dL_dzz1_t2 * c_w61h + c_dL_dzz2_t2 * c_w62h +
                i_dL_dzz1_t2 * i_w61h + i_dL_dzz2_t2 * i_w62h +
                f_dL_dzz1_t2 * f_w61h + f_dL_dzz2_t2 * f_w62h;
        double dL_dh3_t1 =
                o_dL_dzz1_t2 * o_w71h + o_dL_dzz2_t2 * o_w72h +
                c_dL_dzz1_t2 * c_w71h + c_dL_dzz2_t2 * c_w72h +
                i_dL_dzz1_t2 * i_w71h + i_dL_dzz2_t2 * i_w72h +
                f_dL_dzz1_t2 * f_w71h + f_dL_dzz2_t2 * f_w72h;

        double dL_dC1_t1 = dL_dC1_t2 * f1_t2;
        double dL_dC2_t1 = dL_dC2_t2 * f2_t2;
        double dL_dC3_t1 = dL_dC3_t2 * f3_t2;

        // at time 1
        dL_dC1_t1 += dL_dh1_t1 * o1_t1 * (1 - Math.pow(tanh(C1_t1), 2));
        dL_dC2_t1 += dL_dh2_t1 * o2_t1 * (1 - Math.pow(tanh(C2_t1), 2));
        dL_dC3_t1 += dL_dh3_t1 * o3_t1 * (1 - Math.pow(tanh(C3_t1), 2));

        double dL_do1_t1 = dL_dh1_t1 * tanh(C1_t1), dL_do2_t1 = dL_dh2_t1 * tanh(C2_t1), dL_do3_t1 = dL_dh3_t1 * tanh(C3_t1);
        double dL_df1_t1 = dL_dC1_t1 * C1_t0, dL_df2_t1 = dL_dC2_t1 * C2_t0, dL_df3_t1 = dL_dC3_t1 * C3_t0;
        double dL_di1_t1 = dL_dC1_t1 * c1_t1, dL_di2_t1 = dL_dC2_t1 * c2_t1, dL_di3_t1 = dL_dC3_t1 * c3_t1;
        double dL_dc1_t1 = dL_dC1_t1 * i1_t1, dL_dc2_t1 = dL_dC2_t1 * i2_t1, dL_dc3_t1 = dL_dC3_t1 * i3_t1;

        double dL_doz1_t1 = dL_do1_t1 * d_sigmoid(o1_t1), dL_doz2_t1 = dL_do2_t1 * d_sigmoid(o2_t1), dL_doz3_t1 = dL_do3_t1 * d_sigmoid(o3_t1);
        double dL_dwo1h1_t1 = dL_doz1_t1 * oh1_t1 * MRU_LR;
        double dL_dwo1h2_t1 = dL_doz2_t1 * oh1_t1 * MRU_LR;
        double dL_dwo1h3_t1 = dL_doz3_t1 * oh1_t1 * MRU_LR;
        double dL_dwo2h1_t1 = dL_doz1_t1 * oh2_t1 * MRU_LR;
        double dL_dwo2h2_t1 = dL_doz2_t1 * oh2_t1 * MRU_LR;
        double dL_dwo2h3_t1 = dL_doz3_t1 * oh2_t1 * MRU_LR;

        double o_dL_dyz1_t1 = dL_doz1_t1 * o_w1h1 + dL_doz2_t1 * o_w1h2 + dL_doz3_t1 * o_w1h3;
        double o_dL_dzz1_t1 = o_dL_dyz1_t1 * d_sigmoid(oh1_t1);
        double dL_dwo11h_t1 = o_dL_dzz1_t1 * x1_t1 * MRU_LR;
        double dL_dwo21h_t1 = o_dL_dzz1_t1 * x2_t1 * MRU_LR;
        double dL_dwo31h_t1 = o_dL_dzz1_t1 * x3_t1 * MRU_LR;
        double dL_dwo41h_t1 = o_dL_dzz1_t1 * x4_t1 * MRU_LR;
        double dL_dwo51h_t1 = o_dL_dzz1_t1 * h1_t0 * MRU_LR;
        double dL_dwo61h_t1 = o_dL_dzz1_t1 * h2_t0 * MRU_LR;
        double dL_dwo71h_t1 = o_dL_dzz1_t1 * h3_t0 * MRU_LR;

        double o_dL_dyz2_t1 = dL_doz1_t1 * o_w2h1 + dL_doz2_t1 * o_w2h2 + dL_doz3_t1 * o_w2h3;
        double o_dL_dzz2_t1 = o_dL_dyz2_t1 * d_sigmoid(oh2_t1);
        double dL_dwo12h_t1 = o_dL_dzz2_t1 * x1_t1 * MRU_LR;
        double dL_dwo22h_t1 = o_dL_dzz2_t1 * x2_t1 * MRU_LR;
        double dL_dwo32h_t1 = o_dL_dzz2_t1 * x3_t1 * MRU_LR;
        double dL_dwo42h_t1 = o_dL_dzz2_t1 * x4_t1 * MRU_LR;
        double dL_dwo52h_t1 = o_dL_dzz2_t1 * h1_t0 * MRU_LR;
        double dL_dwo62h_t1 = o_dL_dzz2_t1 * h2_t0 * MRU_LR;
        double dL_dwo72h_t1 = o_dL_dzz2_t1 * h3_t0 * MRU_LR;

        double dL_dcz1_t1 = dL_dc1_t1 * d_tanh(c1_t1), dL_dcz2_t1 = dL_dc2_t1 * d_tanh(c2_t1), dL_dcz3_t1 = dL_dc3_t1 * d_tanh(c3_t1);
        double dL_dwc1h1_t1 = dL_dcz1_t1 * ch1_t1 * MRU_LR;
        double dL_dwc1h2_t1 = dL_dcz2_t1 * ch1_t1 * MRU_LR;
        double dL_dwc1h3_t1 = dL_dcz3_t1 * ch1_t1 * MRU_LR;
        double dL_dwc2h1_t1 = dL_dcz1_t1 * ch2_t1 * MRU_LR;
        double dL_dwc2h2_t1 = dL_dcz2_t1 * ch2_t1 * MRU_LR;
        double dL_dwc2h3_t1 = dL_dcz3_t1 * ch2_t1 * MRU_LR;

        double c_dL_dyz1_t1 = dL_dcz1_t1 * c_w1h1 + dL_dcz2_t1 * c_w1h2 + dL_dcz3_t1 * c_w1h3;
        double c_dL_dzz1_t1 = c_dL_dyz1_t1 * d_tanh(ch1_t1);
        double dL_dwc11h_t1 = c_dL_dzz1_t1 * x1_t1 * MRU_LR;
        double dL_dwc21h_t1 = c_dL_dzz1_t1 * x2_t1 * MRU_LR;
        double dL_dwc31h_t1 = c_dL_dzz1_t1 * x3_t1 * MRU_LR;
        double dL_dwc41h_t1 = c_dL_dzz1_t1 * x4_t1 * MRU_LR;
        double dL_dwc51h_t1 = c_dL_dzz1_t1 * h1_t0 * MRU_LR;
        double dL_dwc61h_t1 = c_dL_dzz1_t1 * h2_t0 * MRU_LR;
        double dL_dwc71h_t1 = c_dL_dzz1_t1 * h3_t0 * MRU_LR;

        double c_dL_dyz2_t1 = dL_dcz1_t1 * c_w2h1 + dL_dcz2_t1 * c_w2h2 + dL_dcz3_t1 * c_w2h3;
        double c_dL_dzz2_t1 = c_dL_dyz2_t1 * d_tanh(ch2_t1);
        double dL_dwc12h_t1 = c_dL_dzz2_t1 * x1_t1 * MRU_LR;
        double dL_dwc22h_t1 = c_dL_dzz2_t1 * x2_t1 * MRU_LR;
        double dL_dwc32h_t1 = c_dL_dzz2_t1 * x3_t1 * MRU_LR;
        double dL_dwc42h_t1 = c_dL_dzz2_t1 * x4_t1 * MRU_LR;
        double dL_dwc52h_t1 = c_dL_dzz2_t1 * h1_t0 * MRU_LR;
        double dL_dwc62h_t1 = c_dL_dzz2_t1 * h2_t0 * MRU_LR;
        double dL_dwc72h_t1 = c_dL_dzz2_t1 * h3_t0 * MRU_LR;

        double dL_diz1_t1 = dL_di1_t1 * d_sigmoid(i1_t1), dL_diz2_t1 = dL_di2_t1 * d_sigmoid(i2_t1), dL_diz3_t1 = dL_di3_t1 * d_sigmoid(i3_t1);
        double dL_dwi1h1_t1 = dL_diz1_t1 * ih1_t1 * MRU_LR;
        double dL_dwi1h2_t1 = dL_diz2_t1 * ih1_t1 * MRU_LR;
        double dL_dwi1h3_t1 = dL_diz3_t1 * ih1_t1 * MRU_LR;
        double dL_dwi2h1_t1 = dL_diz1_t1 * ih2_t1 * MRU_LR;
        double dL_dwi2h2_t1 = dL_diz2_t1 * ih2_t1 * MRU_LR;
        double dL_dwi2h3_t1 = dL_diz3_t1 * ih2_t1 * MRU_LR;

        double i_dL_dyz1_t1 = dL_diz1_t1 * i_w1h1 + dL_diz2_t1 * i_w1h2 + dL_diz3_t1 * i_w1h3;
        double i_dL_dzz1_t1 = i_dL_dyz1_t1 * d_sigmoid(ih1_t1);
        double dL_dwi11h_t1 = i_dL_dzz1_t1 * x1_t1 * MRU_LR;
        double dL_dwi21h_t1 = i_dL_dzz1_t1 * x2_t1 * MRU_LR;
        double dL_dwi31h_t1 = i_dL_dzz1_t1 * x3_t1 * MRU_LR;
        double dL_dwi41h_t1 = i_dL_dzz1_t1 * x4_t1 * MRU_LR;
        double dL_dwi51h_t1 = i_dL_dzz1_t1 * h1_t0 * MRU_LR;
        double dL_dwi61h_t1 = i_dL_dzz1_t1 * h2_t0 * MRU_LR;
        double dL_dwi71h_t1 = i_dL_dzz1_t1 * h3_t0 * MRU_LR;

        double i_dL_dyz2_t1 = dL_diz1_t1 * i_w2h1 + dL_diz2_t1 * i_w2h2 + dL_diz3_t1 * i_w2h3;
        double i_dL_dzz2_t1 = i_dL_dyz2_t1 * d_sigmoid(ih2_t1);
        double dL_dwi12h_t1 = i_dL_dzz2_t1 * x1_t1 * MRU_LR;
        double dL_dwi22h_t1 = i_dL_dzz2_t1 * x2_t1 * MRU_LR;
        double dL_dwi32h_t1 = i_dL_dzz2_t1 * x3_t1 * MRU_LR;
        double dL_dwi42h_t1 = i_dL_dzz2_t1 * x4_t1 * MRU_LR;
        double dL_dwi52h_t1 = i_dL_dzz2_t1 * h1_t0 * MRU_LR;
        double dL_dwi62h_t1 = i_dL_dzz2_t1 * h2_t0 * MRU_LR;
        double dL_dwi72h_t1 = i_dL_dzz2_t1 * h3_t0 * MRU_LR;

        double dL_dfz1_t1 = dL_df1_t1 * d_sigmoid(f1_t1), dL_dfz2_t1 = dL_df2_t1 * d_sigmoid(f2_t1), dL_dfz3_t1 = dL_df3_t1 * d_sigmoid(f3_t1);
        double dL_dwf1h1_t1 = dL_dfz1_t1 * fh1_t1 * MRU_LR;
        double dL_dwf1h2_t1 = dL_dfz2_t1 * fh1_t1 * MRU_LR;
        double dL_dwf1h3_t1 = dL_dfz3_t1 * fh1_t1 * MRU_LR;
        double dL_dwf2h1_t1 = dL_dfz1_t1 * fh2_t1 * MRU_LR;
        double dL_dwf2h2_t1 = dL_dfz2_t1 * fh2_t1 * MRU_LR;
        double dL_dwf2h3_t1 = dL_dfz3_t1 * fh2_t1 * MRU_LR;

        double f_dL_dyz1_t1 = dL_dfz1_t1 * f_w1h1 + dL_dfz2_t1 * f_w1h2 + dL_dfz3_t1 * f_w1h3;
        double f_dL_dzz1_t1 = f_dL_dyz1_t1 * d_sigmoid(fh1_t1);
        double dL_dwf11h_t1 = f_dL_dzz1_t1 * x1_t1 * MRU_LR;
        double dL_dwf21h_t1 = f_dL_dzz1_t1 * x2_t1 * MRU_LR;
        double dL_dwf31h_t1 = f_dL_dzz1_t1 * x3_t1 * MRU_LR;
        double dL_dwf41h_t1 = f_dL_dzz1_t1 * x4_t1 * MRU_LR;
        double dL_dwf51h_t1 = f_dL_dzz1_t1 * h1_t0 * MRU_LR;
        double dL_dwf61h_t1 = f_dL_dzz1_t1 * h2_t0 * MRU_LR;
        double dL_dwf71h_t1 = f_dL_dzz1_t1 * h3_t0 * MRU_LR;

        double f_dL_dyz2_t1 = dL_dfz1_t1 * f_w2h1 + dL_dfz2_t1 * f_w2h2 + dL_dfz3_t1 * f_w2h3;
        double f_dL_dzz2_t1 = f_dL_dyz2_t1 * d_sigmoid(fh2_t1);
        double dL_dwf12h_t1 = f_dL_dzz2_t1 * x1_t1 * MRU_LR;
        double dL_dwf22h_t1 = f_dL_dzz2_t1 * x2_t1 * MRU_LR;
        double dL_dwf32h_t1 = f_dL_dzz2_t1 * x3_t1 * MRU_LR;
        double dL_dwf42h_t1 = f_dL_dzz2_t1 * x4_t1 * MRU_LR;
        double dL_dwf52h_t1 = f_dL_dzz2_t1 * h1_t0 * MRU_LR;
        double dL_dwf62h_t1 = f_dL_dzz2_t1 * h2_t0 * MRU_LR;
        double dL_dwf72h_t1 = f_dL_dzz2_t1 * h3_t0 * MRU_LR;

        // GENOME SET UP
        double midX = (inputNodeX + hiddenNodeX) / 2.0;
        MRUNodeGene hid1 = new MRUNodeGene(11), hid2 = new MRUNodeGene(12);
        hid1.setX(midX); hid2.setX(midX);
        g.getNodes().addAll(hid1, hid2);

        MRUConGene c11h = new MRUConGene(in1, hid1), c21h = new MRUConGene(in2, hid1), c31h = new MRUConGene(in3, hid1), c41h = new MRUConGene(in4, hid1);
        MRUConGene c51h = new MRUConGene(in5, hid1), c61h = new MRUConGene(in6, hid1), c71h = new MRUConGene(in7, hid1);
        MRUConGene c12h = new MRUConGene(in1, hid2), c22h = new MRUConGene(in2, hid2), c32h = new MRUConGene(in3, hid2), c42h = new MRUConGene(in4, hid2);
        MRUConGene c52h = new MRUConGene(in5, hid2), c62h = new MRUConGene(in6, hid2), c72h = new MRUConGene(in7, hid2);
        MRUConGene c1h1 = new MRUConGene(hid1, out1), c2h1 = new MRUConGene(hid2, out1);
        MRUConGene c1h2 = new MRUConGene(hid1, out2), c2h2 = new MRUConGene(hid2, out2);
        MRUConGene c1h3 = new MRUConGene(hid1, out3), c2h3 = new MRUConGene(hid2, out3);

        c11h.setWeight(f_w11h, 'f'); c21h.setWeight(f_w21h, 'f'); c31h.setWeight(f_w31h, 'f'); c41h.setWeight(f_w41h, 'f');
        c51h.setWeight(f_w51h, 'f'); c61h.setWeight(f_w61h, 'f'); c71h.setWeight(f_w71h, 'f');
        c12h.setWeight(f_w12h, 'f'); c22h.setWeight(f_w22h, 'f'); c32h.setWeight(f_w32h, 'f'); c42h.setWeight(f_w42h, 'f');
        c52h.setWeight(f_w52h, 'f'); c62h.setWeight(f_w62h, 'f'); c72h.setWeight(f_w72h, 'f');
        c1h1.setWeight(f_w1h1, 'f'); c2h1.setWeight(f_w2h1, 'f');
        c1h2.setWeight(f_w1h2, 'f'); c2h2.setWeight(f_w2h2, 'f');
        c1h3.setWeight(f_w1h3, 'f'); c2h3.setWeight(f_w2h3, 'f');

        c11h.setWeight(i_w11h, 'i'); c21h.setWeight(i_w21h, 'i'); c31h.setWeight(i_w31h, 'i'); c41h.setWeight(i_w41h, 'i');
        c51h.setWeight(i_w51h, 'i'); c61h.setWeight(i_w61h, 'i'); c71h.setWeight(i_w71h, 'i');
        c12h.setWeight(i_w12h, 'i'); c22h.setWeight(i_w22h, 'i'); c32h.setWeight(i_w32h, 'i'); c42h.setWeight(i_w42h, 'i');
        c52h.setWeight(i_w52h, 'i'); c62h.setWeight(i_w62h, 'i'); c72h.setWeight(i_w72h, 'i');
        c1h1.setWeight(i_w1h1, 'i'); c2h1.setWeight(i_w2h1, 'i');
        c1h2.setWeight(i_w1h2, 'i'); c2h2.setWeight(i_w2h2, 'i');
        c1h3.setWeight(i_w1h3, 'i'); c2h3.setWeight(i_w2h3, 'i');

        c11h.setWeight(c_w11h, 'c'); c21h.setWeight(c_w21h, 'c'); c31h.setWeight(c_w31h, 'c'); c41h.setWeight(c_w41h, 'c');
        c51h.setWeight(c_w51h, 'c'); c61h.setWeight(c_w61h, 'c'); c71h.setWeight(c_w71h, 'c');
        c12h.setWeight(c_w12h, 'c'); c22h.setWeight(c_w22h, 'c'); c32h.setWeight(c_w32h, 'c'); c42h.setWeight(c_w42h, 'c');
        c52h.setWeight(c_w52h, 'c'); c62h.setWeight(c_w62h, 'c'); c72h.setWeight(c_w72h, 'c');
        c1h1.setWeight(c_w1h1, 'c'); c2h1.setWeight(c_w2h1, 'c');
        c1h2.setWeight(c_w1h2, 'c'); c2h2.setWeight(c_w2h2, 'c');
        c1h3.setWeight(c_w1h3, 'c'); c2h3.setWeight(c_w2h3, 'c');

        c11h.setWeight(o_w11h, 'o'); c21h.setWeight(o_w21h, 'o'); c31h.setWeight(o_w31h, 'o'); c41h.setWeight(o_w41h, 'o');
        c51h.setWeight(o_w51h, 'o'); c61h.setWeight(o_w61h, 'o'); c71h.setWeight(o_w71h, 'o');
        c12h.setWeight(o_w12h, 'o'); c22h.setWeight(o_w22h, 'o'); c32h.setWeight(o_w32h, 'o'); c42h.setWeight(o_w42h, 'o');
        c52h.setWeight(o_w52h, 'o'); c62h.setWeight(o_w62h, 'o'); c72h.setWeight(o_w72h, 'o');
        c1h1.setWeight(o_w1h1, 'o'); c2h1.setWeight(o_w2h1, 'o');
        c1h2.setWeight(o_w1h2, 'o'); c2h2.setWeight(o_w2h2, 'o');
        c1h3.setWeight(o_w1h3, 'o'); c2h3.setWeight(o_w2h3, 'o');

        c11h.setIN(1); c21h.setIN(2); c31h.setIN(3); c41h.setIN(4); c51h.setIN(5); c61h.setIN(6); c71h.setIN(7);
        c12h.setIN(8); c22h.setIN(9); c32h.setIN(10); c42h.setIN(11); c52h.setIN(12); c62h.setIN(13); c72h.setIN(14);
        c1h1.setIN(15); c2h1.setIN(16);
        c1h2.setIN(17); c2h2.setIN(18);
        c1h3.setIN(19); c2h3.setIN(20);

        hid1.getInCons().addAll(Arrays.asList(c11h, c21h, c31h, c41h, c51h, c61h, c71h));
        hid2.getInCons().addAll(Arrays.asList(c12h, c22h, c32h, c42h, c52h, c62h, c72h));
        out1.getInCons().addAll(Arrays.asList(c1h1, c2h1));
        out2.getInCons().addAll(Arrays.asList(c1h2, c2h2));
        out3.getInCons().addAll(Arrays.asList(c1h3, c2h3));

        g.getCons().addAll(c11h, c21h, c31h, c41h, c51h, c61h, c71h, c12h, c22h, c32h, c42h, c52h, c62h, c72h, c1h1, c2h1, c1h2, c2h2, c1h3, c2h3);
        g.express();

        // TEST FORWARD METHOD
        double[] y_pred = g.feed(inputSeq);

        assertArrayEquals(y, y_pred);

        // TEST BACK PROPAGATION METHOD
        g.train(inputSeq, actorGrads, criticGrads);

        assertDoubleApprox(o_w1h1 + dL_dwo1h1_t1 + dL_dwo1h1_t2, c1h1.getWeight('o'));
        assertDoubleApprox(o_w2h1 + dL_dwo2h1_t1 + dL_dwo2h1_t2, c2h1.getWeight('o'));
        assertDoubleApprox(o_w1h2 + dL_dwo1h2_t1 + dL_dwo1h2_t2, c1h2.getWeight('o'));
        assertDoubleApprox(o_w2h2 + dL_dwo2h2_t1 + dL_dwo2h2_t2, c2h2.getWeight('o'));
        assertDoubleApprox(o_w1h3 + dL_dwo1h3_t1 + dL_dwo1h3_t2, c1h3.getWeight('o'));
        assertDoubleApprox(o_w2h3 + dL_dwo2h3_t1 + dL_dwo2h3_t2, c2h3.getWeight('o'));

        assertDoubleApprox(o_w11h + dL_dwo11h_t1 + dL_dwo11h_t2, c11h.getWeight('o'));
        assertDoubleApprox(o_w21h + dL_dwo21h_t1 + dL_dwo21h_t2, c21h.getWeight('o'));
        assertDoubleApprox(o_w31h + dL_dwo31h_t1 + dL_dwo31h_t2, c31h.getWeight('o'));
        assertDoubleApprox(o_w41h + dL_dwo41h_t1 + dL_dwo41h_t2, c41h.getWeight('o'));
        assertDoubleApprox(o_w51h + dL_dwo51h_t1 + dL_dwo51h_t2, c51h.getWeight('o'));
        assertDoubleApprox(o_w61h + dL_dwo61h_t1 + dL_dwo61h_t2, c61h.getWeight('o'));
        assertDoubleApprox(o_w71h + dL_dwo71h_t1 + dL_dwo71h_t2, c71h.getWeight('o'));

        assertDoubleApprox(o_w12h + dL_dwo12h_t1 + dL_dwo12h_t2, c12h.getWeight('o'));
        assertDoubleApprox(o_w22h + dL_dwo22h_t1 + dL_dwo22h_t2, c22h.getWeight('o'));
        assertDoubleApprox(o_w32h + dL_dwo32h_t1 + dL_dwo32h_t2, c32h.getWeight('o'));
        assertDoubleApprox(o_w42h + dL_dwo42h_t1 + dL_dwo42h_t2, c42h.getWeight('o'));
        assertDoubleApprox(o_w52h + dL_dwo52h_t1 + dL_dwo52h_t2, c52h.getWeight('o'));
        assertDoubleApprox(o_w62h + dL_dwo62h_t1 + dL_dwo62h_t2, c62h.getWeight('o'));
        assertDoubleApprox(o_w72h + dL_dwo72h_t1 + dL_dwo72h_t2, c72h.getWeight('o'));

        assertDoubleApprox(c_w1h1 + dL_dwc1h1_t1 + dL_dwc1h1_t2, c1h1.getWeight('c'));
        assertDoubleApprox(c_w2h1 + dL_dwc2h1_t1 + dL_dwc2h1_t2, c2h1.getWeight('c'));
        assertDoubleApprox(c_w1h2 + dL_dwc1h2_t1 + dL_dwc1h2_t2, c1h2.getWeight('c'));
        assertDoubleApprox(c_w2h2 + dL_dwc2h2_t1 + dL_dwc2h2_t2, c2h2.getWeight('c'));
        assertDoubleApprox(c_w1h3 + dL_dwc1h3_t1 + dL_dwc1h3_t2, c1h3.getWeight('c'));
        assertDoubleApprox(c_w2h3 + dL_dwc2h3_t1 + dL_dwc2h3_t2, c2h3.getWeight('c'));

        assertDoubleApprox(c_w11h + dL_dwc11h_t1 + dL_dwc11h_t2, c11h.getWeight('c'));
        assertDoubleApprox(c_w21h + dL_dwc21h_t1 + dL_dwc21h_t2, c21h.getWeight('c'));
        assertDoubleApprox(c_w31h + dL_dwc31h_t1 + dL_dwc31h_t2, c31h.getWeight('c'));
        assertDoubleApprox(c_w41h + dL_dwc41h_t1 + dL_dwc41h_t2, c41h.getWeight('c'));
        assertDoubleApprox(c_w51h + dL_dwc51h_t1 + dL_dwc51h_t2, c51h.getWeight('c'));
        assertDoubleApprox(c_w61h + dL_dwc61h_t1 + dL_dwc61h_t2, c61h.getWeight('c'));
        assertDoubleApprox(c_w71h + dL_dwc71h_t1 + dL_dwc71h_t2, c71h.getWeight('c'));

        assertDoubleApprox(c_w12h + dL_dwc12h_t1 + dL_dwc12h_t2, c12h.getWeight('c'));
        assertDoubleApprox(c_w22h + dL_dwc22h_t1 + dL_dwc22h_t2, c22h.getWeight('c'));
        assertDoubleApprox(c_w32h + dL_dwc32h_t1 + dL_dwc32h_t2, c32h.getWeight('c'));
        assertDoubleApprox(c_w42h + dL_dwc42h_t1 + dL_dwc42h_t2, c42h.getWeight('c'));
        assertDoubleApprox(c_w52h + dL_dwc52h_t1 + dL_dwc52h_t2, c52h.getWeight('c'));
        assertDoubleApprox(c_w62h + dL_dwc62h_t1 + dL_dwc62h_t2, c62h.getWeight('c'));
        assertDoubleApprox(c_w72h + dL_dwc72h_t1 + dL_dwc72h_t2, c72h.getWeight('c'));

        assertDoubleApprox(i_w1h1 + dL_dwi1h1_t1 + dL_dwi1h1_t2, c1h1.getWeight('i'));
        assertDoubleApprox(i_w2h1 + dL_dwi2h1_t1 + dL_dwi2h1_t2, c2h1.getWeight('i'));
        assertDoubleApprox(i_w1h2 + dL_dwi1h2_t1 + dL_dwi1h2_t2, c1h2.getWeight('i'));
        assertDoubleApprox(i_w2h2 + dL_dwi2h2_t1 + dL_dwi2h2_t2, c2h2.getWeight('i'));
        assertDoubleApprox(i_w1h3 + dL_dwi1h3_t1 + dL_dwi1h3_t2, c1h3.getWeight('i'));
        assertDoubleApprox(i_w2h3 + dL_dwi2h3_t1 + dL_dwi2h3_t2, c2h3.getWeight('i'));

        assertDoubleApprox(i_w11h + dL_dwi11h_t1 + dL_dwi11h_t2, c11h.getWeight('i'));
        assertDoubleApprox(i_w21h + dL_dwi21h_t1 + dL_dwi21h_t2, c21h.getWeight('i'));
        assertDoubleApprox(i_w31h + dL_dwi31h_t1 + dL_dwi31h_t2, c31h.getWeight('i'));
        assertDoubleApprox(i_w41h + dL_dwi41h_t1 + dL_dwi41h_t2, c41h.getWeight('i'));
        assertDoubleApprox(i_w51h + dL_dwi51h_t1 + dL_dwi51h_t2, c51h.getWeight('i'));
        assertDoubleApprox(i_w61h + dL_dwi61h_t1 + dL_dwi61h_t2, c61h.getWeight('i'));
        assertDoubleApprox(i_w71h + dL_dwi71h_t1 + dL_dwi71h_t2, c71h.getWeight('i'));

        assertDoubleApprox(i_w12h + dL_dwi12h_t1 + dL_dwi12h_t2, c12h.getWeight('i'));
        assertDoubleApprox(i_w22h + dL_dwi22h_t1 + dL_dwi22h_t2, c22h.getWeight('i'));
        assertDoubleApprox(i_w32h + dL_dwi32h_t1 + dL_dwi32h_t2, c32h.getWeight('i'));
        assertDoubleApprox(i_w42h + dL_dwi42h_t1 + dL_dwi42h_t2, c42h.getWeight('i'));
        assertDoubleApprox(i_w52h + dL_dwi52h_t1 + dL_dwi52h_t2, c52h.getWeight('i'));
        assertDoubleApprox(i_w62h + dL_dwi62h_t1 + dL_dwi62h_t2, c62h.getWeight('i'));
        assertDoubleApprox(i_w72h + dL_dwi72h_t1 + dL_dwi72h_t2, c72h.getWeight('i'));

        assertDoubleApprox(f_w1h1 + dL_dwf1h1_t1 + dL_dwf1h1_t2, c1h1.getWeight('f'));
        assertDoubleApprox(f_w2h1 + dL_dwf2h1_t1 + dL_dwf2h1_t2, c2h1.getWeight('f'));
        assertDoubleApprox(f_w1h2 + dL_dwf1h2_t1 + dL_dwf1h2_t2, c1h2.getWeight('f'));
        assertDoubleApprox(f_w2h2 + dL_dwf2h2_t1 + dL_dwf2h2_t2, c2h2.getWeight('f'));
        assertDoubleApprox(f_w1h3 + dL_dwf1h3_t1 + dL_dwf1h3_t2, c1h3.getWeight('f'));
        assertDoubleApprox(f_w2h3 + dL_dwf2h3_t1 + dL_dwf2h3_t2, c2h3.getWeight('f'));

        assertDoubleApprox(f_w11h + dL_dwf11h_t1 + dL_dwf11h_t2, c11h.getWeight('f'));
        assertDoubleApprox(f_w21h + dL_dwf21h_t1 + dL_dwf21h_t2, c21h.getWeight('f'));
        assertDoubleApprox(f_w31h + dL_dwf31h_t1 + dL_dwf31h_t2, c31h.getWeight('f'));
        assertDoubleApprox(f_w41h + dL_dwf41h_t1 + dL_dwf41h_t2, c41h.getWeight('f'));
        assertDoubleApprox(f_w51h + dL_dwf51h_t1 + dL_dwf51h_t2, c51h.getWeight('f'));
        assertDoubleApprox(f_w61h + dL_dwf61h_t1 + dL_dwf61h_t2, c61h.getWeight('f'));
        assertDoubleApprox(f_w71h + dL_dwf71h_t1 + dL_dwf71h_t2, c71h.getWeight('f'));

        assertDoubleApprox(f_w12h + dL_dwf12h_t1 + dL_dwf12h_t2, c12h.getWeight('f'));
        assertDoubleApprox(f_w22h + dL_dwf22h_t1 + dL_dwf22h_t2, c22h.getWeight('f'));
        assertDoubleApprox(f_w32h + dL_dwf32h_t1 + dL_dwf32h_t2, c32h.getWeight('f'));
        assertDoubleApprox(f_w42h + dL_dwf42h_t1 + dL_dwf42h_t2, c42h.getWeight('f'));
        assertDoubleApprox(f_w52h + dL_dwf52h_t1 + dL_dwf52h_t2, c52h.getWeight('f'));
        assertDoubleApprox(f_w62h + dL_dwf62h_t1 + dL_dwf62h_t2, c62h.getWeight('f'));
        assertDoubleApprox(f_w72h + dL_dwf72h_t1 + dL_dwf72h_t2, c72h.getWeight('f'));

    }


}
