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
        vOrder.setLayout(new BoxLayout(vOrder, BoxLayout.Y_AXIS));
        vOrder.setAlignmentY(Component.TOP_ALIGNMENT);
        vOrder.setBorder(BorderFactory.createEmptyBorder(0,15,0,0));

        //Left Side
        vOrder.add(new JLabel(type));
        vOrder.add(Box.createRigidArea(new Dimension(0,5)));

        if(!type.equals("Output")) {
            vOrder.add(new JLabel("Function:"));
            vOrder.add(makeTextField(150, 24));
            vOrder.add(Box.createRigidArea(new Dimension(0,5)));
            vOrder.add(new JLabel("Period:"));
            vOrder.add(makeTextField(150, 24));
            vOrder.add(Box.createRigidArea(new Dimension(0,5)));
            vOrder.add(new JLabel(type + " Frequency:"));
            vOrder.add(makeTextField(150, 24));
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
            vOrder.add(play);
        }

        if(type.equals("Modulator")) {
            vOrder.add(new JLabel("Modulation Sensitivity:"));
            vOrder.add(makeTextField(150, 24));
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

    private JTextField makeTextField(int width, int height) {
        JTextField tf = new JTextField();
        Dimension d = new Dimension(width, height);
        tf.setPreferredSize(d);
        tf.setMaximumSize(d);     // prevents BoxLayout from stretching it
        tf.setMinimumSize(d);
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
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

    }
}
