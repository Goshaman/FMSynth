import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Function;
public class Params {
    private final String carrier_;
    private final String modulator_;
    private final String periodC_;
    private final String periodM_;
    private final double fc_;
    private final double fm_;
    private final double kf_;

    public Params(String carrier, String modulator, String periodC, String periodM, double fc, double fm, double kf) {
        carrier_ = carrier;
        modulator_ = modulator;
        periodC_ = periodC;
        periodM_ = periodM;
        fc_ = fc;
        fm_ = fm;
        kf_ = kf;
    }

    public String getCarrier() {return carrier_;}
    public String getModulator() {return modulator_;}
    public String getPeriodC() {return periodC_;}
    public String getPeriodM() {return periodM_;}
    public double getFc() {return fc_;}
    public double getFm() {return fm_;}
    public double getKf() {return kf_;}

    @Override
    public String toString() {
        return "yabadabadoo";
    }
}
