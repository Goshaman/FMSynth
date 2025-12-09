import javax.swing.*;
import java.awt.*;

public class OscilloscopePanel extends JPanel {
    private short[] samples;
    private Color waveColor = new Color(80, 220, 140);
    private Color gridColor = new Color(50, 52, 58);
    private Color bgColor = new Color(25, 27, 32);
    private Color borderColor = new Color(60, 62, 68);

    public OscilloscopePanel() {
        setBackground(bgColor);
        samples = new short[800];
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
    }

    public void updateSamples(short[] newSamples) {
        if (newSamples != null && newSamples.length > 0) {
            samples = newSamples.clone();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Insets insets = getInsets();
        int x0 = insets.left;
        int y0 = insets.top;
        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;
        int centerY = y0 + height / 2;

        // Draw grid
        g2d.setColor(gridColor);

        // Vertical grid lines
        g2d.setStroke(new BasicStroke(0.5f));
        for (int i = 0; i <= 10; i++) {
            int x = x0 + (i * width / 10);
            g2d.drawLine(x, y0, x, y0 + height);
        }

        // Horizontal grid lines
        for (int i = 0; i <= 4; i++) {
            int y = y0 + (i * height / 4);
            if (i == 2) {
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.setColor(new Color(70, 72, 78));
            } else {
                g2d.setStroke(new BasicStroke(0.5f));
                g2d.setColor(gridColor);
            }
            g2d.drawLine(x0, y, x0 + width, y);
        }

        // Draw waveform
        if (samples != null && samples.length > 1) {
            // Glow effect
            for (int pass = 3; pass >= 1; pass--) {
                int alpha = 25 * pass;
                g2d.setColor(new Color(waveColor.getRed(), waveColor.getGreen(), waveColor.getBlue(), alpha));
                g2d.setStroke(new BasicStroke(2.0f * pass, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawWaveform(g2d, x0, y0, width, height, centerY);
            }

            // Main waveform line
            g2d.setColor(waveColor);
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawWaveform(g2d, x0, y0, width, height, centerY);
        }

        // Draw label with subtle background
        g2d.setColor(new Color(35, 37, 42, 200));
        g2d.fillRoundRect(x0 + 5, y0 + 5, 95, 20, 4, 4);

        g2d.setColor(new Color(180, 182, 188));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2d.drawString("OSCILLOSCOPE", x0 + 10, y0 + 19);
    }

    private void drawWaveform(Graphics2D g2d, int x0, int y0, int width, int height, int centerY) {
        int numSamples = samples.length;
        double xScale = (double) width / numSamples;
        double yScale = (height / 2.0 - 10);

        int[] xPoints = new int[numSamples];
        int[] yPoints = new int[numSamples];

        for (int i = 0; i < numSamples; i++) {
            xPoints[i] = x0 + (int)(i * xScale);
            yPoints[i] = centerY - (int)((samples[i] / 32768.0) * yScale);
        }

        g2d.drawPolyline(xPoints, yPoints, numSamples);
    }
}