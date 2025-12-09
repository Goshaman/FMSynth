import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private ArrayList<Operator> operators; //store all operators
    private ArrayList<OperatorPanel> operatorPanels; //store all visual panels (the function rectangle)
    private JPanel operatorsPanel; //other methods can use
    private JScrollPane operatorsScroll;
    private double minOperatorPercent = 0.20;  // minimum height before scroll appears
    private Synthesis synth;
    private boolean isResizing = false; // prevent infinite resize loop
    private ModMatrixPanel modMatrixPanel;
    private OscilloscopePanel oscilloscope;
    private Timer oscilloscopeTimer;

    private int borderWidth = 3;
    private int panelHeight = 360;
    private int panelWidth = 1600;

    public MainFrame(Synthesis s) {
        synth = s;
        setTitle("TrottelSynth");
        setSize(panelWidth, 400*3 +50);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        setLayout(new BorderLayout());

        //for operators, going on the left
        operatorsPanel = new JPanel();
        operatorsPanel.setLayout(new BoxLayout(operatorsPanel, BoxLayout.Y_AXIS));
        operatorsPanel.setBackground(new Color(45, 45, 50));
        //new lists
        operators = new ArrayList<Operator>();
        operatorPanels = new ArrayList<OperatorPanel>();

        //for mod matrix + oscilloscope
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(45, 45, 50));
        // mod matrix area, top part of right
        modMatrixPanel = new ModMatrixPanel(this);
        // oscilloscope area
        oscilloscope = new OscilloscopePanel();
        oscilloscope.setPreferredSize(new Dimension(0, 150));
        //add matrix and oscilloscope to rightPanel
        rightPanel.add(modMatrixPanel, BorderLayout.CENTER);
        rightPanel.add(oscilloscope, BorderLayout.SOUTH);

        //keyboard
        KeyboardPanel keyboardPanel = new KeyboardPanel(this, synth);
        keyboardPanel.setPreferredSize(new Dimension(0, getHeight()/6));

        //for 50 50 split
        JPanel topPanel = new JPanel(new GridLayout(1,2));
        topPanel.setBackground(new Color(45, 45, 50));

        //button + scrollable operators
        JPanel operatorsWrapper = new JPanel(new BorderLayout());
        operatorsWrapper.setBackground(new Color(45, 45, 50));

        //add operator button
        JButton addButton = new JButton("+ Operator");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setBackground(new Color(60, 60, 65));
        addButton.setForeground(new Color(200, 200, 210));
        addButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        addButton.addActionListener(new java.awt.event.ActionListener() { // listens for button cl icks
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
        operatorsScroll.setBackground(new Color(45, 45, 50));
        operatorsScroll.setBorder(null);
        operatorsScroll.getViewport().setBackground(new Color(45, 45, 50));
        operatorsWrapper.add(operatorsScroll, BorderLayout.CENTER);

        //base = 3: always starts with 3 operators
        addOperator();
        addOperator();
        addOperator();
        synth.setOperators(operators);
        modMatrixPanel.updateMatrix(operators);
        synth.setModMatrix(modMatrixPanel.getMatrixValues());
        topPanel.add(operatorsWrapper);

        //matrix and output
        topPanel.add(rightPanel);

        //add the entire upper thing to topPanel
        add(topPanel, BorderLayout.CENTER);
        add(keyboardPanel, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                // Prevent infinite resize loop
                if (isResizing) return;
                isResizing = true;

                int width = getWidth();
                int height = getHeight();

                // Update keyboard height
                int keyboardHeight = height / 6;
                if (keyboardHeight < 80) keyboardHeight = 80;
                keyboardPanel.setPreferredSize(new Dimension(0, keyboardHeight));

                // Update oscilloscope height
                int oscilloscopeHeight = height / 5;
                if (oscilloscopeHeight < 100) oscilloscopeHeight = 100;
                oscilloscope.setPreferredSize(new Dimension(0, oscilloscopeHeight));

                resizeOperatorPanels();
                revalidate();

                isResizing = false;
            }
        });

        // Start global refresh timer (updates oscilloscope and all visuals)
        oscilloscopeTimer = new Timer(50, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // Only update oscilloscope via timer (canvases update on change)
                if (oscilloscope != null) {
                    oscilloscope.updateSamples(synth.getSamples());
                }
            }
        });
        oscilloscopeTimer.start();

        setVisible(true);
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
        synth.setOperators(operators);
        modMatrixPanel.updateMatrix(operators);
        synth.setModMatrix(modMatrixPanel.getMatrixValues());
    }

    public void removeOperator(int id) {
        // don't allow fewer than 2 operators
        if (operators.size() <= 1) {
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
        synth.setOperators(operators);
        modMatrixPanel.updateMatrix(operators);
        synth.setModMatrix(modMatrixPanel.getMatrixValues());
    }

    public void updateSynthesis() {
        synth.setOperators(operators);
        if (modMatrixPanel != null) {
            synth.setModMatrix(modMatrixPanel.getMatrixValues());
        }
    }

    public void globalRefresh() {
        // Update synthesis
        synth.setOperators(operators);
        if (modMatrixPanel != null) {
            synth.setModMatrix(modMatrixPanel.getMatrixValues());
        }

        // Update all operator canvases
        for (OperatorPanel opPanel : operatorPanels) {
            opPanel.refreshCanvas();
        }

        // Update oscilloscope
        if (oscilloscope != null) {
            oscilloscope.updateSamples(synth.getSamples());
        }
    }

    // Getter for operators (so Synthesis can access them)
    public ArrayList<Operator> getOperators() {
        return operators;
    }
}