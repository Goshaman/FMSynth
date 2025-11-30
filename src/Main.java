import java.math.*;
import javax.sound.sampled.*;
import javax.swing.*;

import org.mariuszgromada.math.mxparser.*;

public class Main {
    public static void main(String[] args) throws Exception {
        License.iConfirmNonCommercialUse("Gosha Rassokhin");


//        short[] audio = generateSamples(44100, 3.0, carrier2, modulator2, fc.getArgumentValue(), fm.getArgumentValue(), kf.getArgumentValue());
//        System.out.println("soy done");
//        playStored(audio, 44100);
        Params def = new Params(
                "sin(t)",
                "sin(t)",
                "2 * pi",
                "2 * pi",
                200,
                0,
                0
        );
        Synthesis synthesizer = new Synthesis(def);
        MainFrame frame = new MainFrame(synthesizer);
    }

    public static double[] extrema(Function a, int sampleF, double period) {
        double[] out = new double[2];

        double max = a.calculate(0);
        double min = a.calculate(0);

        for(int i = 1; i <= sampleF; i++) {
            double cur = a.calculate(i * period / sampleF);
            max = Math.max(max, cur);
            min = Math.min(min, cur);
        }

        out[0] = max;
        out[1] = min;

        return out;
    }

    static short[] generateSamples(
            int sampleRate,
            double duration,
            Function carrier2,
            Function modulator2,
            double fc,
            double fm,
            double kf
    ) {

        int totalSamples = (int)(sampleRate * duration);
        short[] samples = new short[totalSamples];

        double dt = 1.0 / sampleRate;

        double integral = 0.0;       // I(t)
        double prevMod = modulator2.calculate(0);   // f(0)

        for (int i = 0; i < totalSamples; i++) {
            double t = i * dt;

            // Evaluate modulator at current time
            double mod = modulator2.calculate(t * fm);    // f(n)

            // Trapezoidal integration:
            //
            // I[n] = I[n-1] + (prevMod + mod) * dt / 2
            //
            integral += (prevMod + mod) * dt * 0.5;

            // Compute FM-modulated phase
            double phase = fc * t + kf * integral;

            // Evaluate carrier using this phase
            double value = carrier2.calculate(phase);

            // Clamp and convert to 16-bit PCM
            if (!Double.isFinite(value)) value = 0;
            value = Math.max(-1, Math.min(1, value));

            samples[i] = (short)(value * Short.MAX_VALUE);

            // Update for next step
            prevMod = mod;
        }

        return samples;
    }


    static void playStored(short[] samples, int sampleRate) throws Exception {
        AudioFormat format = new AudioFormat(
                sampleRate, 16, 1, true, false
        );
        SourceDataLine line = AudioSystem.getSourceDataLine(format);

        line.open(format);
        line.start();

        byte[] buffer = new byte[samples.length * 2];

        for (int i = 0; i < samples.length; i++) {
            short v = samples[i];
            buffer[2*i] = (byte)(v & 0xFF);          // little endian
            buffer[2*i+1] = (byte)((v >> 8) & 0xFF);
        }

        line.write(buffer, 0, buffer.length);
        line.drain();
        line.stop();
        line.close();
    }
}


