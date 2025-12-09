import javax.sound.sampled.*;
import org.mariuszgromada.math.mxparser.*;
import java.util.ArrayList;

public class Synthesis {
    private SourceDataLine line;
    private Thread audioThread;
    private volatile boolean running = false;
    private volatile boolean playing = false;
    private volatile double frequency = 440;
    private final int SAMPLE_RATE = 44100;
    private short[] lastSamples;
    private final int BUFFER_SIZE = 800;
    private ArrayList<Operator> operators;
    private ArrayList<Function> operatorFunctions;
    private volatile int[][] modMatrix;

    public void setOperators(ArrayList<Operator> ops) {
        operators = ops;
        // Pre-compile functions once
        operatorFunctions = new ArrayList<>();
        if (operators != null) {
            for (Operator op : operators) {
                operatorFunctions.add(new Function("f(t) = " + op.getFunction()));
            }
        }
    }

    public void setModMatrix(int[][] matrix) {
        modMatrix = matrix;
    }

    public Synthesis() {
        lastSamples = new short[BUFFER_SIZE];
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, 8192);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (running) return;
        running = true;
        line.start();

        audioThread = new Thread(new Runnable() {
            public void run() {
                audioLoop();
            }
        });
        audioThread.start();
    }

    public void setFrequency(double freq) {
        frequency = freq;
    }

    public void playSignal() {
        playing = true;
    }

    public void stopSignal() {
        playing = false;
    }

    public void updateParams(Params p) {
        // Placeholder
    }

    public short[] getSamples() {
        return lastSamples.clone();
    }

    public double eval(double in, String type) {
        return Math.sin(in);
    }

    private void audioLoop() {
        double[] phases = new double[10]; // max 10 operators
        double dt = 1.0 / SAMPLE_RATE;
        byte[] buffer = new byte[2048];

        while (running) {
            int bufferIdx = 0;
            int sampleIdx = 0;
            for (int i = 0; i < 1024; i++) {
                double value = 0;

                if (playing) {
                    if (operatorFunctions != null && operatorFunctions.size() > 0 && operators != null) {
                        int numOps = operators.size();
                        double[] opOutputs = new double[numOps];

                        // First pass: calculate modulators (no modulation applied)
                        for (int opIdx = 0; opIdx < numOps; opIdx++) {
                            Operator op = operators.get(opIdx);
                            if (!op.isCarrier()) {
                                Function func = operatorFunctions.get(opIdx);
                                double opFreq = op.getFrequency();

                                // Modulators don't receive modulation
                                double t = phases[opIdx] * 2 * Math.PI;
                                opOutputs[opIdx] = func.calculate(t);

                                if (!Double.isFinite(opOutputs[opIdx])) {
                                    opOutputs[opIdx] = 0;
                                }

                                // Update phase for modulator
                                phases[opIdx] += opFreq * dt;
                                if (phases[opIdx] > 1) phases[opIdx] -= 1;
                            }
                        }

                        // Second pass: calculate carriers (with modulation from modulators)
                        for (int opIdx = 0; opIdx < numOps; opIdx++) {
                            Operator op = operators.get(opIdx);
                            if (op.isCarrier()) {
                                Function func = operatorFunctions.get(opIdx);
                                double opFreq = frequency; // Carriers use keyboard frequency

                                // Calculate modulation from modulators
                                double modulation = 0;
                                if (modMatrix != null && modMatrix.length == numOps) {
                                    for (int modIdx = 0; modIdx < numOps; modIdx++) {
                                        if (modIdx != opIdx && modMatrix[modIdx][opIdx] != 0) {
                                            // modIdx modulates opIdx
                                            double modDepth = modMatrix[modIdx][opIdx] / 10.0; // scale mod index
                                            modulation += opOutputs[modIdx] * modDepth;
                                        }
                                    }
                                }

                                // Apply phase modulation (FM synthesis)
                                double t = (phases[opIdx] + modulation) * 2 * Math.PI;
                                opOutputs[opIdx] = func.calculate(t);

                                if (!Double.isFinite(opOutputs[opIdx])) {
                                    opOutputs[opIdx] = 0;
                                }

                                // Update phase for carrier
                                phases[opIdx] += opFreq * dt;
                                if (phases[opIdx] > 1) phases[opIdx] -= 1;
                            }
                        }

                        // Third pass: sum carrier outputs
                        int carrierCount = 0;
                        for (int opIdx = 0; opIdx < numOps; opIdx++) {
                            if (operators.get(opIdx).isCarrier()) {
                                value += opOutputs[opIdx];
                                carrierCount++;
                            }
                        }

                        // Normalize by carrier count
                        if (carrierCount > 0) {
                            value /= carrierCount;
                        }
                    } else {
                        // Fallback to sine if no operators
                        value = Math.sin(phases[0] * 2 * Math.PI);
                        phases[0] += frequency * dt;
                        if (phases[0] > 1) phases[0] -= 1;
                    }
                }

                short sample = (short)(value * Short.MAX_VALUE * 0.15);

                if (sampleIdx < BUFFER_SIZE) {
                    lastSamples[sampleIdx++] = sample;
                }

                buffer[bufferIdx++] = (byte)(sample & 0xFF);
                buffer[bufferIdx++] = (byte)((sample >> 8) & 0xFF);
            }

            line.write(buffer, 0, buffer.length);
        }
    }
}