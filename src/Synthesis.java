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
        double phase = 0;
        double dt = 1.0 / SAMPLE_RATE;
        byte[] buffer = new byte[2048];

        while (running) {
            int bufferIdx = 0;
            int sampleIdx = 0;
            for (int i = 0; i < 1024; i++) {
                double value = 0;

                if (playing) {
                    if (operatorFunctions != null && operatorFunctions.size() > 0) {
                        // Sum all operators (treat them all as carriers for now)
                        double t = phase * 2 * Math.PI;
                        for (Function func : operatorFunctions) {
                            double opValue = func.calculate(t);
                            if (Double.isFinite(opValue)) {
                                value += opValue / operatorFunctions.size();
                            }
                        }
                    } else {
                        // Fallback to sine if no operators
                        value = Math.sin(phase * 2 * Math.PI);
                    }
                    phase += frequency * dt;
                    if (phase > 1) phase -= 1;
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