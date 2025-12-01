import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Function;

public class Synthesis {
    private Function carrier, modulator, carrier2, modulator2;
    private Argument periodC, periodM, fc, fm, kf;
    private short[] audio;
    public Synthesis(){

    }

    public short[] getSamples() {return audio;}
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

    public void play() throws Exception {
        Main.playStored(audio, 44100);
    }
}
