import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import org.mariuszgromada.math.mxparser.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    public ArrayList<JPanel> panels;
    public JPanel mainPanel;

    private int borderWidth = 3;
    private int panelHeight = 400;
    private int panelWidth = 1600;
    public MainFrame() {
        setTitle("FMSynth");
        setSize(panelWidth, 1200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.NORTH);

        panels = new ArrayList<JPanel>();
        CreatePanel();
        CreatePanel();
    }

    public void CreatePanel() {
        JPanel panel = new JPanel();
        panels.add(panel);
        //panel.setBounds(10,10 * panels.size() + (panels.size() - 1) * panelHeight,panelWidth - 30 - borderWidth * 2,panelHeight);
        panel.setPreferredSize(new Dimension(panelWidth - 30 - borderWidth * 2, panelHeight));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setLayout(new BorderLayout());

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
        vOrder.add(new JLabel("Carrier"));
        vOrder.add(Box.createRigidArea(new Dimension(0,5)));
        vOrder.add(new JLabel("Function:"));
        vOrder.add(makeTextField(150, 24));
        vOrder.add(Box.createRigidArea(new Dimension(0,5)));
        vOrder.add(new JLabel("Period:"));
        vOrder.add(makeTextField(150, 24));
        vOrder.add(Box.createRigidArea(new Dimension(0,5)));
        vOrder.add(new JLabel("Carrier Frequency:"));
        vOrder.add(makeTextField(150, 24));

        Canvas canvas = new Canvas();
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

    public void updateCanvas() {
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
}
