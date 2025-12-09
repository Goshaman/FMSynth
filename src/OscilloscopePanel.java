import javax.swing.*;
import java.awt.*;

public class OscilloscopePanel extends JPanel {
    private short[] samples;
    private Color waveColor = new Color(0, 255, 100);
    private Color gridColor = new Color(80, 80, 80);

    public OscilloscopePanel() {
        setBackground(Color.BLACK);
        samples = new short[800];
    }

    public void updateSamples(short[] newSamples) {
        if (newSamples != null && newSamples.length > 0) {
            samples = newSamples.clone();
            repaint();
        }
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;

        // Draw grid
        g2d.setColor(gridColor);
        // Horizontal center line
        g2d.drawLine(0, centerY, width, centerY);
        // Horizontal quarter lines
        g2d.drawLine(0, height / 4, width, height / 4);
        g2d.drawLine(0, 3 * height / 4, width, 3 * height / 4);
        // Vertical lines
        for (int i = 0; i <= 4; i++) {
            int x = i * width / 4;
            g2d.drawLine(x, 0, x, height);
        }

        // Draw waveform
        if (samples != null && samples.length > 1) {
            g2d.setColor(waveColor);
            g2d.setStroke(new BasicStroke(1.0f));

            int numSamples = samples.length;
            double xScale = (double) width / numSamples;

            for (int i = 0; i < numSamples - 1; i++) {
                int x1 = (int) (i * xScale);
                int x2 = (int) ((i + 1) * xScale);

                // Scale sample values to panel height
                // samples are in range [-32768, 32767]
                double y1 = centerY - (samples[i] / 32768.0) * (height / 2 - 10);
                double y2 = centerY - (samples[i + 1] / 32768.0) * (height / 2 - 10);

                g2d.drawLine(x1, (int) y1, x2, (int) y2);
            }
        }

        // Draw label
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.drawString("Oscilloscope", 10, 20);
    }
}
