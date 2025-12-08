import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Function;
import javax.sound.sampled.*;
import java.util.Arrays;

public class Synthesis {
    private static final int NUM_OPERATORS = 6;
    private static final int SAMPLE_RATE = 44100;
    private static final double FADE_STEP = 0.0005;

    private SourceDataLine line;
    private Thread audioThread;

    private volatile Function[] operators;
    private volatile Function[] normalizedOperators;
    private volatile Argument[] periods;
    private volatile double[] frequencies;
    private volatile double[][] modulationMatrix;

    private short[] audio;

    private volatile boolean running = false;
    private volatile boolean shouldStop = false;

    private double currentVolume = 0.0;
    private double targetVolume = 1.0;

    public Synthesis(Params p) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, 32768);  // Increased buffer size from 8192 to 32768
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        operators = new Function[NUM_OPERATORS];
        normalizedOperators = new Function[NUM_OPERATORS];
        periods = new Argument[NUM_OPERATORS];
        frequencies = new double[NUM_OPERATORS];
        modulationMatrix = new double[NUM_OPERATORS][NUM_OPERATORS];

        updateParams(p);
    }

    public short[] getSamples() { return audio; }

    public double eval(double in, String type) {
        if (type.equals("Carrier")) {
            return normalizedOperators[0] != null ? normalizedOperators[0].calculate(in) : 0;
        } else if (type.equals("Modulator")) {
            return normalizedOperators[1] != null ? normalizedOperators[1].calculate(in) : 0;
        } else if (type.startsWith("Op")) {
            try {
                int opNum = Integer.parseInt(type.substring(2));
                if (opNum >= 0 && opNum < NUM_OPERATORS && normalizedOperators[opNum] != null) {
                    return normalizedOperators[opNum].calculate(in);
                }
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public void updateParams(Params p) {
        for (int i = 0; i < NUM_OPERATORS; i++) {
            operators[i] = new Function("op" + i + "(t) = " + p.getOperatorFunction(i));
            periods[i] = new Argument("period" + i + " = " + p.getPeriod(i));
            frequencies[i] = p.getFrequency(i);
        }

        double[][] matrix = p.getModulationMatrix();
        for (int i = 0; i < NUM_OPERATORS; i++) {
            for (int j = 0; j < NUM_OPERATORS; j++) {
                modulationMatrix[i][j] = matrix[i][j];
            }
        }

        synthesize();
    }

    public void start() {
        if (running) return;
        running = true;
        shouldStop = false;

        currentVolume = 0.0;
        targetVolume = 1.0;

        line.write(new byte[4096], 0, 4096);  // Pre-fill with larger buffer
        line.start();

        audioThread = new Thread(this::audioLoop);
        audioThread.setPriority(Thread.MAX_PRIORITY);
        audioThread.start();
    }

    public void close() {
        shouldStop = true;
        try {
            if (audioThread != null) audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (line != null) {
            line.stop();
            line.close();
        }
        running = false;
    }

    public void synthesize() {
        for (int i = 0; i < NUM_OPERATORS; i++) {
            double[] extrema = Main.extrema(operators[i], 120, periods[i].getArgumentValue());
            Argument max = new Argument("max" + i, extrema[0]);
            Argument min = new Argument("min" + i, extrema[1]);

            normalizedOperators[i] = new Function("norm" + i + "(t) = 2 * ((op" + i + "(mod(period" + i + " * t, period" + i + ")) - min" + i + ")/(max" + i + " - min" + i + ")) - 1");
            normalizedOperators[i].addArguments(periods[i], min, max);
            normalizedOperators[i].addFunctions(operators[i]);
        }

        System.out.println("starting sample generation");
        audio = generateSamples(SAMPLE_RATE, 3.0);
        System.out.println("ending sample generation");
    }

    public void stopSignal() {
        targetVolume = 0.0;
    }

    public void playSignal() {
        targetVolume = 1.0;
    }

    private void audioLoop() {
        double dt = 1.0 / SAMPLE_RATE;
        double[] phases = new double[NUM_OPERATORS];
        byte[] buffer = new byte[8192];  // Increased from 2048 to 8192 bytes (4096 samples)

        while (!shouldStop) {
            Function[] locOps = normalizedOperators.clone();
            double[] locFreqs = frequencies.clone();
            double[][] locMatrix = new double[NUM_OPERATORS][NUM_OPERATORS];
            for (int i = 0; i < NUM_OPERATORS; i++) {
                locMatrix[i] = modulationMatrix[i].clone();
            }

            if (currentVolume <= 0.0001 && targetVolume == 0.0) {
                currentVolume = 0.0;
                Arrays.fill(buffer, (byte)0);
                line.write(buffer, 0, buffer.length);
                continue;
            }

            boolean allNull = true;
            for (Function op : locOps) {
                if (op != null) {
                    allNull = false;
                    break;
                }
            }

            if (allNull) {
                Arrays.fill(buffer, (byte)0);
                line.write(buffer, 0, buffer.length);
                continue;
            }

            int bufferIdx = 0;

            for (int i = 0; i < 4096; i++) {  // Increased from 1024 to 4096 samples
                if (currentVolume < targetVolume) {
                    currentVolume += FADE_STEP;
                    if (currentVolume > targetVolume) currentVolume = targetVolume;
                } else if (currentVolume > targetVolume) {
                    currentVolume -= FADE_STEP;
                    if (currentVolume < targetVolume) currentVolume = targetVolume;
                }

                double[] modulations = new double[NUM_OPERATORS];

                for (int to = 0; to < NUM_OPERATORS; to++) {
                    double totalMod = 0;
                    for (int from = 0; from < NUM_OPERATORS; from++) {
                        if (locOps[from] != null && locMatrix[from][to] != 0) {
                            double modOutput = locOps[from].calculate(phases[from]);
                            totalMod += modOutput * locMatrix[from][to];
                        }
                    }
                    modulations[to] = totalMod;
                }

                double[] opOutputs = new double[NUM_OPERATORS];
                for (int op = 0; op < NUM_OPERATORS; op++) {
                    if (locOps[op] != null) {
                        double modulatedPhase = phases[op] + modulations[op];
                        opOutputs[op] = locOps[op].calculate(modulatedPhase);
                    }
                }

                for (int op = 0; op < NUM_OPERATORS; op++) {
                    phases[op] += locFreqs[op] * dt;
                }

                double val = opOutputs[0];

                if (!Double.isFinite(val)) val = 0;
                val = Math.max(-1, Math.min(1, val));
                val *= currentVolume;

                short sample = (short) (val * Short.MAX_VALUE);

                buffer[bufferIdx++] = (byte) (sample & 0xFF);
                buffer[bufferIdx++] = (byte) ((sample >> 8) & 0xFF);
            }

            line.write(buffer, 0, buffer.length);
        }
    }

    private short[] generateSamples(int sampleRate, double duration) {
        int totalSamples = (int)(sampleRate * duration);
        short[] samples = new short[totalSamples];
        double dt = 1.0 / sampleRate;
        double[] phases = new double[NUM_OPERATORS];

        for (int i = 0; i < totalSamples; i++) {
            double[] modulations = new double[NUM_OPERATORS];

            for (int to = 0; to < NUM_OPERATORS; to++) {
                double totalMod = 0;
                for (int from = 0; from < NUM_OPERATORS; from++) {
                    if (normalizedOperators[from] != null && modulationMatrix[from][to] != 0) {
                        double modOutput = normalizedOperators[from].calculate(phases[from]);
                        totalMod += modOutput * modulationMatrix[from][to];
                    }
                }
                modulations[to] = totalMod;
            }

            double[] opOutputs = new double[NUM_OPERATORS];
            for (int op = 0; op < NUM_OPERATORS; op++) {
                if (normalizedOperators[op] != null) {
                    double modulatedPhase = phases[op] + modulations[op];
                    opOutputs[op] = normalizedOperators[op].calculate(modulatedPhase);
                }
            }

            for (int op = 0; op < NUM_OPERATORS; op++) {
                phases[op] += frequencies[op] * dt;
            }

            double value = opOutputs[0];

            if (!Double.isFinite(value)) value = 0;
            value = Math.max(-1, Math.min(1, value));

            samples[i] = (short)(value * Short.MAX_VALUE);
        }

        return samples;
    }
}
