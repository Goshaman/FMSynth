import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import org.mariuszgromada.math.mxparser.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    public ArrayList<JPanel> panels;
    public JPanel mainPanel;
    private Synthesis synth;

    private int borderWidth = 3;
    private int panelHeight = 400;
    private int panelWidth = 1600;
    public MainFrame(Synthesis s) {
        setTitle("FMSynth");
        setSize(panelWidth, 400*3 +50);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.NORTH);

        panels = new ArrayList<JPanel>();
        CreatePanel("Carrier");
        CreatePanel("Modulator");
        CreatePanel("Output");

        synth = s;
    }

    public void CreatePanel(String type) {
        JPanel panel = new JPanel();
        panels.add(panel);
        //panel.setBounds(10,10 * panels.size() + (panels.size() - 1) * panelHeight,panelWidth - 30 - borderWidth * 2,panelHeight);
        panel.setPreferredSize(new Dimension(panelWidth - 30 - borderWidth * 2, panelHeight));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setLayout(new BorderLayout());
        panel.setName(type);

        Border lineBorder = new LineBorder(Color.GRAY, borderWidth, false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10,10,10,10),
                lineBorder
        ));

        JPanel hOrder = new JPanel();
        hOrder.setLayout(new BoxLayout(hOrder, BoxLayout.X_AXIS));

        JPanel vOrder = new JPanel();
        vOrder.setName("Vertical Order");
        vOrder.setLayout(new BoxLayout(vOrder, BoxLayout.Y_AXIS));
        vOrder.setAlignmentY(Component.TOP_ALIGNMENT);
        vOrder.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));

        //Left Side
        vOrder.add(new JLabel(type));
        vOrder.add(Box.createRigidArea(new Dimension(0,5)));

        if(!type.equals("Output")) {
            vOrder.add(new JLabel("Function:"));
            vOrder.add(makeTextField(150, 24, type));
            vOrder.add(Box.createRigidArea(new Dimension(0,5)));
            vOrder.add(new JLabel("Period:"));
            if(type.equals("Carrier")) {
                vOrder.add(makeTextField(150, 24, "periodC"));
            } else {
                vOrder.add(makeTextField(150, 24, "periodM"));
            }
            vOrder.add(Box.createRigidArea(new Dimension(0,5)));
            vOrder.add(new JLabel(type + " Frequency:"));
            if(type.equals("Carrier")) {
                vOrder.add(makeTextField(150, 24, "fc"));
            } else {
                vOrder.add(makeTextField(150, 24, "fm"));
            }
        } else {
            JButton redraw = new JButton("Redraw");
            Dimension buttonSize = new Dimension(100, 24);
            redraw.setPreferredSize(buttonSize);
            redraw.setMaximumSize(buttonSize);
            redraw.setMinimumSize(buttonSize);
            redraw.setBackground(Color.GREEN);
            redraw.setOpaque(true);
            redraw.setFocusPainted(false);
            redraw.setBorderPainted(false);

            redraw.addActionListener(e -> {remake();});

            JButton play = new JButton("Play");
            play.setPreferredSize(buttonSize);
            play.setMaximumSize(buttonSize);
            play.setMinimumSize(buttonSize);
            play.setBackground(Color.GREEN);
            play.setOpaque(true);
            play.setFocusPainted(false);
            play.setBorderPainted(false);

            play.addActionListener(e -> {
                try {
                    synth.play();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

            vOrder.add(redraw);
            vOrder.add(Box.createRigidArea(new Dimension(0,5)));
            vOrder.add(play);
        }

        if(type.equals("Modulator")) {
            vOrder.add(new JLabel("Modulation Sensitivity:"));
            vOrder.add(makeTextField(150, 24, "kf"));
        }

        Canvas canvas = new Canvas(type);
        canvas.setPreferredSize(new Dimension(0, panelHeight - 20));
        canvas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10,10,10,10),
                BorderFactory.createLineBorder(Color.GRAY, 2)
        ));

        hOrder.add(vOrder);
        hOrder.add(Box.createRigidArea(new Dimension(10, 0))); // gap
        hOrder.add(canvas);
        hOrder.setName("Horizontal Order");

        panel.add(hOrder, BorderLayout.CENTER);
        panel.setMaximumSize(panel.getPreferredSize());

        mainPanel.add(panel);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JTextField makeTextField(int width, int height, String name) {
        JTextField tf = new JTextField();
        Dimension d = new Dimension(width, height);
        tf.setPreferredSize(d);
        tf.setMaximumSize(d);     // prevents BoxLayout from stretching it
        tf.setMinimumSize(d);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setName(name);
        return tf;
    }

    private void updateCanvas() {
        for(JPanel i: panels) {
            Canvas canvas;
            for(Component a: i.getComponents()) {
                if(a instanceof JPanel) {
                    JPanel hOr = (JPanel) a;
                    for(Component b: hOr.getComponents()) {
                        if(b instanceof Canvas) {
                            canvas = (Canvas) b;
                            canvas.setName("Canvas");

                            canvas.repaint();
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    private void remake() {
        // Audio

        String carrierFunction = null, modulatorFunction = null, periodC = null, periodM = null;
        double fc = 0, fm = 0, kf = 0;

        for(JPanel panel: panels) {
            for(Component a: panel.getComponents()) {
                if (a instanceof JPanel) {
                    JPanel hOr = (JPanel) a;
                    if (hOr.getName().equals("Horizontal Order")) {
                        for (Component b : hOr.getComponents()) {
                            if (b instanceof JPanel) {
                                JPanel vOr = (JPanel) b;
                                if (vOr.getName().equals("Vertical Order")) {
                                    for(Component field: vOr.getComponents()) {
                                        if(field instanceof JTextField) {
                                            JTextField textField = (JTextField) field;
                                            switch(textField.getName()) {
                                                case "Carrier":
                                                    carrierFunction = textField.getText();
                                                    break;
                                                case "Modulator":
                                                    modulatorFunction = textField.getText();
                                                    break;
                                                case "periodC":
                                                    periodC = textField.getText();
                                                    break;
                                                case "periodM":
                                                    periodM = textField.getText();
                                                    break;
                                                case "fc":
                                                    fc = Double.parseDouble(textField.getText());
                                                    break;
                                                case "fm":
                                                    fm = Double.parseDouble(textField.getText());
                                                    break;
                                                case "kf":
                                                    kf = Double.parseDouble(textField.getText());
                                                    break;
                                                default:
                                                    break;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        Params params = new Params(carrierFunction, modulatorFunction, periodC, periodM, fc, fm, kf);
        System.out.println(params);
        synth.updateParams(params);

        // Graphing

        for(JPanel i: panels) {
            Canvas canvas = null;
            for(Component a: i.getComponents()) {
                if(a instanceof JPanel) {
                    JPanel hOr = (JPanel) a;
                    for(Component b: hOr.getComponents()) {
                        if(b instanceof Canvas) {
                            canvas = (Canvas) b;
                        }
                    }
                    break;
                }
            }
            if(canvas.getType().equals("Carrier")) {
                canvas.changeParam(synth, periodC, fc);
            } else if (canvas.getType().equals("Modulator")) {
                canvas.changeParam(synth, periodM, fm);
            } else {
                canvas.changeParam(synth.getSamples(), periodC, 200);
            }
        }
        updateCanvas();
    }
}
