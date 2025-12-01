import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Function;
import javax.sound.sampled.*;
import java.util.Arrays;

public class Synthesis {
    private SourceDataLine line;
    private Thread audioThread;
    private volatile Function carrier, modulator, carrier2, modulator2;
    private volatile Argument periodC, periodM, fc, fm, kf;
    private short[] audio;

    // Flags to control the background thread
    private volatile boolean running = false;
    private volatile boolean shouldStop = false;
    private volatile boolean signalActive = true;

    private double currentVolume = 0.0;
    private double targetVolume = 1.0;
    // Fade step per sample: 0.0005 means it takes 2000 samples (~45ms) to fade fully
    // This is fast enough to feel instant, but slow enough to remove clicks.
    private final double FADE_STEP = 0.0005;
    private final int SAMPLE_RATE = 44100;
    public Synthesis(Params p){
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(format);
            // 8192 buffer offers a good balance of low latency and stability
            line.open(format, 8192);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        updateParams(p);
    }

    public short[] getSamples() {return audio;}

    //public short[] getSamples() {return audio;}
    public double eval(double in, String type) {
        if(type.equals("Carrier")) {
            return carrier2.calculate(in);
        } else {
            return modulator2.calculate(in);
        }
    }

    public void updateParams(Params p) {
        carrier = new Function("carrier(t) = " + p.getCarrier());
        modulator = new Function("modulator(t) = " + p.getModulator());
        periodC = new Argument("periodC = " + p.getPeriodC());
        periodM = new Argument("periodM = " + p.getPeriodM());
        fc = new Argument("fc = " + p.getFc());
        fm = new Argument("fm = " + p.getFm());
        kf = new Argument("kf = " + p.getKf());

        synthesize();
    }

    public void start() {
        if (running) return;
        running = true;
        shouldStop = false;

        currentVolume = 0.0;
        targetVolume = 1.0;

        // Prime the line with a tiny bit of silence to prevent startup click
        line.write(new byte[1024], 0, 1024);
        line.start();

        // Start the background generation loop
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
        double[] extremaC = Main.extrema(carrier, 120, periodC.getArgumentValue());
        Argument maxC = new Argument("maxC");
        Argument minC = new Argument("minC");

        maxC.setArgumentValue(extremaC[0]);
        minC.setArgumentValue(extremaC[1]);

        carrier2 = new Function("carrier2(t) = 2 * ((carrier(mod(periodC * t, periodC)) - minC)/(maxC - minC)) - 1");
        carrier2.addArguments(periodC, minC, maxC);
        carrier2.addFunctions(carrier);

        double[] extremaM = Main.extrema(modulator, 120, periodM.getArgumentValue());
        Argument maxM = new Argument("maxM");
        Argument minM = new Argument("minM");

        maxM.setArgumentValue(extremaM[0]);
        minM.setArgumentValue(extremaM[1]);

        modulator2 = new Function("modulator2(t) = 2 * ((modulator(mod(periodM * t, periodM)) - minM)/(maxM - minM)) - 1");
        modulator2.addArguments(periodM, minM, maxM);
        modulator2.addFunctions(modulator);

        Function output = new Function("output(t) = carrier2(fc * t + kf * int(modulator2(tau * fm), tau, 0, t))");
        output.addArguments(kf, fm, fc);
        output.addFunctions(carrier2, modulator2);

        System.out.println("starting sample generation");
        audio = Main.generateSamples(44100, 3.0, carrier2, modulator2, fc.getArgumentValue(), fm.getArgumentValue(), kf.getArgumentValue());
        System.out.println("ending sample generation");
    }

    public void stopSignal() {
        targetVolume = 0.0;
    }

    /**
     * Unmutes the sound.
     * Use this to start hearing the signal again.
     */
    public void playSignal() {
        targetVolume = 1.0;
    }

    private void audioLoop() {
        double dt = 1.0 / SAMPLE_RATE;

        double carrierPhase = 0.0;
        double modulatorPhase = 0.0;

        byte[] buffer = new byte[2048]; // 1024 samples

        while (!shouldStop) {
            Function locCar = carrier2;
            Function locMod = modulator2;
            double locFc = fc.getArgumentValue();
            double locFm = fm.getArgumentValue();
            double locKf = kf.getArgumentValue();

            // Optimization: If completely silent AND target is silence, write 0s and skip math.
            // This effectively pauses the phase calculations while muted.
            if (currentVolume <= 0.0001 && targetVolume == 0.0) {
                currentVolume = 0.0; // Clamp to exact 0
                Arrays.fill(buffer, (byte)0);
                line.write(buffer, 0, buffer.length);
                continue;
            }

            // If parameters are missing, treat as silence
            if (locCar == null || locMod == null) {
                Arrays.fill(buffer, (byte)0);
                line.write(buffer, 0, buffer.length);
                continue;
            }

            int bufferIdx = 0;

            // Generate 1024 samples
            for (int i = 0; i < 1024; i++) {

                // --- Volume Ramping Logic ---
                if (currentVolume < targetVolume) {
                    currentVolume += FADE_STEP;
                    if (currentVolume > targetVolume) currentVolume = targetVolume;
                } else if (currentVolume > targetVolume) {
                    currentVolume -= FADE_STEP;
                    if (currentVolume < targetVolume) currentVolume = targetVolume;
                }
                // -----------------------------

                // 1. Calculate Modulator
                double modVal = locMod.calculate(modulatorPhase);
                modulatorPhase += locFm * dt;

                // 2. Calculate Carrier
                double instFreq = locFc + locKf * modVal;
                double val = locCar.calculate(carrierPhase);
                carrierPhase += instFreq * dt;

                // 3. Apply Volume & Process Output
                if (!Double.isFinite(val)) val = 0;
                val = Math.max(-1, Math.min(1, val));

                // Apply the envelope
                val *= currentVolume;

                short sample = (short) (val * Short.MAX_VALUE);

                buffer[bufferIdx++] = (byte) (sample & 0xFF);
                buffer[bufferIdx++] = (byte) ((sample >> 8) & 0xFF);
            }

            // Write chunk to the sound card
            line.write(buffer, 0, buffer.length);
        }
    }

//    public void play() throws Exception {
//        Main.generateAndPlay(44100, 3.0, carrier2, modulator2, fc.getArgumentValue(), fm.getArgumentValue(), kf.getArgumentValue());
//    }
}
