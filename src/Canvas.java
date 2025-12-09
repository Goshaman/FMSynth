import javax.swing.*;
import java.awt.*;
import org.mariuszgromada.math.mxparser.*;

public class Canvas extends JPanel {
    private int panelHeight;
    private int panelWidth;
    private Function function;
    private double period;
    private double fPeriod;
    private double freq = 440.0;
    private String type_;
    private short[] audioSamples;
    private Synthesis synth_;

    // Colors
    private Color bgColor = new Color(28, 30, 36);
    private Color gridColor = new Color(50, 52, 58);
    private Color axisColor = new Color(70, 72, 78);
    private Color waveColor = new Color(100, 180, 255);

    public Canvas(String type) {
        function = new Function("s(t) = sin(t)");
        period = 2 * Math.PI;
        fPeriod = period * 4;
        type_ = type;
        audioSamples = null;
        setBackground(bgColor);
    }

    public String getType() {
        return type_;
    }

    public void changeParam(Synthesis synth, String newPeriod, double newFreq) {
        Argument sub = new Argument("a = " + newPeriod);
        period = sub.getArgumentValue();
        freq = newFreq;
        fPeriod = freq / 50.0;
        synth_ = synth;
        function = null;
        repaint();
    }

    public void changeParam(short[] s, String newPeriod, double newFreq) {
        audioSamples = s;
        Argument sub = new Argument("a = " + newPeriod);
        period = sub.getArgumentValue();
        freq = newFreq;
        fPeriod = freq / 50.0;
        repaint();
    }

    public void setFunction(String func, String per) {
        function = new Function("f(t) = " + func);
        Argument p = new Argument("p = " + per);
        period = p.getArgumentValue();
        fPeriod = period * 4;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Insets insets = getInsets();
        int x0 = insets.left + 5;
        int y0 = insets.top + 5;
        panelWidth = getWidth() - insets.left - insets.right - 10;
        panelHeight = getHeight() - insets.top - insets.bottom - 10;

        if (panelWidth <= 0 || panelHeight <= 0) return;

        int centerY = y0 + panelHeight / 2;

        // Draw subtle grid
        g2d.setColor(gridColor);
        g2d.setStroke(new BasicStroke(0.5f));

        // Vertical lines
        for (int i = 0; i <= 8; i++) {
            int x = x0 + (i * panelWidth / 8);
            g2d.drawLine(x, y0, x, y0 + panelHeight);
        }

        // Horizontal lines
        for (int i = 0; i <= 4; i++) {
            int y = y0 + (i * panelHeight / 4);
            g2d.drawLine(x0, y, x0 + panelWidth, y);
        }

        // Draw axes
        g2d.setColor(axisColor);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine(x0, centerY, x0 + panelWidth, centerY);  // X axis
        g2d.drawLine(x0, y0, x0, y0 + panelHeight);           // Y axis

        // Draw waveform
        g2d.setColor(waveColor);
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int samples = type_.equals("Output") ? 800 : 300;
        double yScale = (panelHeight / 2.0 - 5);

        int[] xPoints = new int[samples];
        int[] yPoints = new int[samples];

        for (int i = 0; i < samples; i++) {
            xPoints[i] = x0 + (int)((double)i * panelWidth / samples);

            double value = 0;
            if (type_.equals("Output") && audioSamples != null && i < audioSamples.length) {
                value = audioSamples[i] / 32768.0;
            } else if (function != null) {
                double t = (double)i * fPeriod / samples;
                value = function.calculate(t);
                if (!Double.isFinite(value)) value = 0;
            } else if (synth_ != null) {
                value = synth_.eval((double)i * fPeriod / samples, type_);
            }

            yPoints[i] = centerY - (int)(value * yScale);
        }

        // Draw with slight glow
        g2d.setColor(new Color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), 60));
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawPolyline(xPoints, yPoints, samples);

        g2d.setColor(waveColor);
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawPolyline(xPoints, yPoints, samples);
    }
}