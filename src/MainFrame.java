import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import org.mariuszgromada.math.mxparser.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private ArrayList<Operator> operators; //store all operators
    private ArrayList<OperatorPanel> operatorPanels; //store all visual panels (the functon rectangle)
    private JPanel operatorsPanel; //other methods can use
    private JScrollPane operatorsScroll;
    private double minOperatorPercent = 0.20;  // minimum height before scroll appears
    public ArrayList<JPanel> panels;
    public JPanel mainPanel;
    private Synthesis synth;

    private int borderWidth = 3;
    private int panelHeight = 360;
    private int panelWidth = 1600;
    public MainFrame(Synthesis s) {
        setTitle("FMSynth");
        setSize(panelWidth, 400*3 +50);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        //commented at 9pm 12/7 to use borderlayout
        //mainPanel = new JPanel();
        //mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        //add(mainPanel, BorderLayout.NORTH);
        setLayout(new BorderLayout());

        //for operators, going on the left
        operatorsPanel = new JPanel();
        operatorsPanel.setLayout(new BoxLayout(operatorsPanel, BoxLayout.Y_AXIS));
        operatorsPanel.setBackground(Color.LIGHT_GRAY);
        //new lists
        operators = new ArrayList<Operator>();
        operatorPanels = new ArrayList<OperatorPanel>();



        //for mod matrix + output (or oscilloscope)
        JPanel rightPanel = new JPanel(new BorderLayout());
        // mod matrix area, top part of right
        JPanel modMatrixPanel = new JPanel();
        modMatrixPanel.setBackground(Color.GRAY);
        // output area
        final JPanel outputPanel = new JPanel(); //final lets inner class (component listener) access this
        outputPanel.setBackground(Color.LIGHT_GRAY);
        //add matrix and output to rightPanel
        rightPanel.add(modMatrixPanel, BorderLayout.CENTER);
        rightPanel.add(outputPanel, BorderLayout.SOUTH);

        //keyboard
        JPanel keyboardPanel = new JPanel();
        keyboardPanel.setPreferredSize(new Dimension(0, getHeight()/6));
        keyboardPanel.setBackground(Color.DARK_GRAY);

        //for 50 50 split
        JPanel topPanel = new JPanel(new GridLayout(1,2));

        //button + scrollable operators
        JPanel operatorsWrapper = new JPanel(new BorderLayout());

        //add operator button
        JButton addButton = new JButton("+ Operator");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(new java.awt.event.ActionListener() { // listens for button clicks
            public void actionPerformed(java.awt.event.ActionEvent e) { //runs when button is clicked
                if (operators.size() < 10) { //max operators = 10
                    addOperator();
                }
            }
        });
        operatorsWrapper.add(addButton, BorderLayout.NORTH);

        //scrollable operators
        operatorsScroll = new JScrollPane(operatorsPanel);
        operatorsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        operatorsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        operatorsWrapper.add(operatorsScroll, BorderLayout.CENTER);

        //base = 3: always starts with 3 operators
        addOperator();
        addOperator();
        addOperator();

        topPanel.add(operatorsWrapper);

        //matrix and output
        topPanel.add(rightPanel);

        //add the entire upper thing to topPanel
        add(topPanel, BorderLayout.CENTER);
        add(keyboardPanel, BorderLayout.SOUTH);
        panels = new ArrayList<JPanel>();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = getWidth();
                int targetHeight = (width * 3) / 4;  // 4:3 ratio
                if (getHeight() != targetHeight) {
                    setSize(width, targetHeight);
                }
                keyboardPanel.setPreferredSize(new Dimension(0, getHeight() / 6));
                outputPanel.setPreferredSize(new Dimension(0, getHeight() / 5));
                resizeOperatorPanels();
                revalidate();
            }
        });
        setVisible(true);


        //old code
        //CreatePanel("Carrier");
        //CreatePanel("Modulator");
        //CreatePanel("Output");

        synth = s;
    }

    private void resizeOperatorPanels() {
        // get available height (scroll area height)
        int availableHeight = operatorsScroll.getViewport().getHeight();
        int availableWidth = operatorsScroll.getViewport().getWidth();

        // height per operator
        int numOperators = operatorPanels.size();
        if (numOperators == 0) {
            return;  // nothing to resize
        }

        int minHeight = (int)(availableHeight * minOperatorPercent);

        // Calculate height per operator
        int heightPerOperator = availableHeight / numOperators;

        // calculated height is less than minimum = use minimum (scroll will appear)
        if (heightPerOperator < minHeight) {
            heightPerOperator = minHeight;
        }

        // height change
        for (int i = 0; i < operatorPanels.size(); i++) {
            OperatorPanel panel = operatorPanels.get(i);
            Dimension size = new Dimension(availableWidth - 20, heightPerOperator);
            panel.setPreferredSize(size);
            panel.setMinimumSize(size);
            panel.setMaximumSize(size);
        }

        int totalHeight = heightPerOperator * numOperators;
        if (totalHeight < availableHeight) {
            totalHeight = availableHeight; //fill available height
            heightPerOperator = availableHeight / numOperators;
            for (int i = 0; i < operatorPanels.size(); i++) {
                OperatorPanel panel = operatorPanels.get(i);
                Dimension size = new Dimension(availableWidth - 20, heightPerOperator);
                panel.setPreferredSize(size);
                panel.setMinimumSize(size);
                panel.setMaximumSize(size);
            }
        }
        operatorsPanel.setPreferredSize(new Dimension(availableWidth, totalHeight));

        // recalculate size
        operatorsPanel.revalidate();
        operatorsPanel.repaint();
    }

    private void addOperator() {
        int id = operators.size() + 1; //for mod matrix purposes, makes each unique
        Operator op = new Operator(id);
        //store in operators list
        operators.add(op);
        //make panel for it in the left panel
        OperatorPanel panel = new OperatorPanel(op, this);
        operatorPanels.add(panel);
        //add panel to operators AREA (operatorsPanel singular is the area)
        operatorsPanel.add(panel);
        //refresh
        operatorsPanel.revalidate();
        operatorsPanel.repaint();
        resizeOperatorPanels();
    }
    public void removeOperator(int id) {
        // don't allow fewer than 2 operators
        if (operators.size() <= 2) {
            return;
        }

        // find and remove the operator with this id
        int indexToRemove = -1;
        for (int i = 0; i < operators.size(); i++) {
            if (operators.get(i).getId() == id) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove == -1) {
            return;  // not found
        }

        // remove from lists
        operators.remove(indexToRemove);
        OperatorPanel panelToRemove = operatorPanels.get(indexToRemove);
        operatorPanels.remove(indexToRemove);

        // remove from screen
        operatorsPanel.remove(panelToRemove);

        // renumber remaining operators
        for (int i = 0; i < operators.size(); i++) {
            operators.get(i).setId(i + 1);
        }

        // rebuild all panels with new numbers
        operatorsPanel.removeAll();
        operatorPanels.clear();
        for (int i = 0; i < operators.size(); i++) {
            Operator op = operators.get(i);
            OperatorPanel panel = new OperatorPanel(op, this);
            operatorPanels.add(panel);
            operatorsPanel.add(panel);
        }

        // refresh
        resizeOperatorPanels();
        operatorsPanel.revalidate();
        operatorsPanel.repaint();
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
                    synth.playSignal();
                    Thread.sleep(1000);
                    synth.stopSignal();
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
