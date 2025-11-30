import javax.swing.*;
import java.awt.*;
import org.mariuszgromada.math.mxparser.*;
public class Canvas extends JPanel {

    private int panelHeight;
    private int panelWidth;
    private int dS = 12; // Dead space --> combination of empty border as well as border thickness
    private Function a;
    private double period;
    private double fPeriod;
    private double freq;
    private int periodMinimizer = 50;
    private String type_;
    public Canvas(String type) {
        a = new Function("s(t) = sin(t)");
        period = 2 * Math.PI;
        fPeriod = freq * period / periodMinimizer;
        type_ = type;
    }

    public String getType() {return type_;}
    public void changeParam(Function newFunction,double newPeriod, double newFreq) {
        a = newFunction;
        period = newPeriod;
        freq = newFreq;

        fPeriod = freq * period / periodMinimizer;
    }
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Insets insets = getInsets();

        panelHeight = getHeight() - insets.top - insets.bottom;
        panelWidth = getWidth() - insets.left - insets.right;

        super.paintComponent(g); // clear the panel

        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(dS + 15, dS + 15, dS + 15, dS + panelHeight - 15);
        g2d.drawLine(dS + 15, dS + panelHeight / 2, dS + panelWidth - 15, dS + panelHeight / 2);

        g2d.setColor(Color.BLUE);
        int x_offset = dS + 15;
        int y_offset = dS + panelHeight / 2;
        double[] prev = {x_offset, y_offset - (a.calculate(0) * (panelHeight / 2.0 - 15))};
        double[] curr = new double[2];
        int samples = 44100;

        for(int i = 1; i <= samples; i++) {
            curr[0] = x_offset + i * (panelWidth - 30) / samples;
            curr[1] = y_offset - (a.calculate(i * fPeriod / samples) * (panelHeight / 2.0 - 15));
            g2d.drawLine((int)prev[0], (int)prev[1], (int)curr[0], (int)curr[1]);
            prev[0] = curr[0];
            prev[1] = curr[1];
        }
    }
}
