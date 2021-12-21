package NEAT.Genome;

public class Adam {

    public static class Moment {

        public int t;
        public double m, v;

        public Moment() {
            t = 0;
            m = v = 0.0;
        }

        @Override
        public Moment clone() throws CloneNotSupportedException {                        // CONNECTION MOMENTUM IS NOT BEING PASSED DONN GENERATION
            Moment clone = new Moment();
            clone.t = t;
            clone.m = m;
            clone.v = v;
            return clone;
        }

    }

    public static double optimize(double g, Moment moment) {

        // NOTE when optimize is invoked, we progress to next time step
        moment.t += 1;

        moment.m = 0.9 * moment.m + 0.1 * g; // momentum
        moment.v = 0.999 * moment.v + 0.001 * g * g; // RMSProp

        // bias correction
        double m_hat = moment.m / (1 - Math.pow(0.9, moment.t));
        double v_hat = moment.v / (1 - Math.pow(0.999, moment.t));

        return 1E-3 * m_hat / (Math.sqrt(v_hat) + 1E-8);

    }

}
