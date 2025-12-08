import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Function;

public class Params {
    private static final int NUM_OPERATORS = 6;

    private final String[] operatorFunctions_;
    private final String[] periods_;
    private final double[] frequencies_;
    private final double[][] modulationMatrix_; // [from][to] - how much operator[from] modulates operator[to]

    public Params(String[] operatorFunctions, String[] periods, double[] frequencies, double[][] modulationMatrix) {
        if (operatorFunctions.length != NUM_OPERATORS || periods.length != NUM_OPERATORS ||
            frequencies.length != NUM_OPERATORS || modulationMatrix.length != NUM_OPERATORS) {
            throw new IllegalArgumentException("All arrays must have " + NUM_OPERATORS + " elements");
        }

        operatorFunctions_ = operatorFunctions.clone();
        periods_ = periods.clone();
        frequencies_ = frequencies.clone();
        modulationMatrix_ = new double[NUM_OPERATORS][NUM_OPERATORS];
        for (int i = 0; i < NUM_OPERATORS; i++) {
            modulationMatrix_[i] = modulationMatrix[i].clone();
        }
    }

    // Legacy constructor for backwards compatibility
    public Params(String carrier, String modulator, String periodC, String periodM, double fc, double fm, double kf) {
        operatorFunctions_ = new String[NUM_OPERATORS];
        periods_ = new String[NUM_OPERATORS];
        frequencies_ = new double[NUM_OPERATORS];
        modulationMatrix_ = new double[NUM_OPERATORS][NUM_OPERATORS];

        // Set up first two operators as carrier and modulator
        operatorFunctions_[0] = carrier;
        operatorFunctions_[1] = modulator;
        periods_[0] = periodC;
        periods_[1] = periodM;
        frequencies_[0] = fc;
        frequencies_[1] = fm;

        // Initialize remaining operators with defaults
        for (int i = 2; i < NUM_OPERATORS; i++) {
            operatorFunctions_[i] = "sin(t)";
            periods_[i] = "2 * pi";
            frequencies_[i] = 440.0;
        }

        // Set up modulation matrix: operator 1 modulates operator 0 with strength kf
        modulationMatrix_[1][0] = kf;
    }

    public static int getNumOperators() { return NUM_OPERATORS; }

    public String getOperatorFunction(int index) { return operatorFunctions_[index]; }
    public String getPeriod(int index) { return periods_[index]; }
    public double getFrequency(int index) { return frequencies_[index]; }
    public double getModulation(int from, int to) { return modulationMatrix_[from][to]; }
    public double[][] getModulationMatrix() {
        double[][] copy = new double[NUM_OPERATORS][NUM_OPERATORS];
        for (int i = 0; i < NUM_OPERATORS; i++) {
            copy[i] = modulationMatrix_[i].clone();
        }
        return copy;
    }

    // Legacy getters for backwards compatibility
    public String getCarrier() { return operatorFunctions_[0]; }
    public String getModulator() { return operatorFunctions_[1]; }
    public String getPeriodC() { return periods_[0]; }
    public String getPeriodM() { return periods_[1]; }
    public double getFc() { return frequencies_[0]; }
    public double getFm() { return frequencies_[1]; }
    public double getKf() { return modulationMatrix_[1][0]; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Operators: ").append(NUM_OPERATORS).append("\n");
        for (int i = 0; i < NUM_OPERATORS; i++) {
            sb.append("Op").append(i).append(": ")
              .append(operatorFunctions_[i]).append(", ")
              .append(periods_[i]).append(", ")
              .append(frequencies_[i]).append("\n");
        }
        sb.append("Modulation Matrix:\n");
        for (int i = 0; i < NUM_OPERATORS; i++) {
            for (int j = 0; j < NUM_OPERATORS; j++) {
                if (modulationMatrix_[i][j] != 0) {
                    sb.append("  Op").append(i).append("->Op").append(j)
                      .append(": ").append(modulationMatrix_[i][j]).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
