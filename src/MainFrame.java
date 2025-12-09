import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private ArrayList<Operator> operators;
    private ArrayList<OperatorPanel> operatorPanels;
    private JPanel operatorsPanel;
    private JScrollPane operatorsScroll;
    private double minOperatorPercent = 0.22;
    private Synthesis synth;
    private boolean isResizing = false;
    private ModMatrixPanel modMatrixPanel;
    private OscilloscopePanel oscilloscope;
    private Timer oscilloscopeTimer;
    private KeyboardPanel keyboardPanel;

    // Colors
    private Color bgColor = new Color(38, 40, 45);
    private Color panelBgColor = new Color(45, 47, 52);

    public MainFrame(Synthesis s) {
        synth = s;
        setTitle("TrottelSynth");
        setSize(1400, 900);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // Set dark look
        getContentPane().setBackground(bgColor);
        setLayout(new BorderLayout(0, 0));

        // Initialize lists
        operators = new ArrayList<>();
        operatorPanels = new ArrayList<>();

        // ========== OPERATORS PANEL (LEFT) ==========
        operatorsPanel = new JPanel();
        operatorsPanel.setLayout(new BoxLayout(operatorsPanel, BoxLayout.Y_AXIS));
        operatorsPanel.setBackground(panelBgColor);
        operatorsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel operatorsWrapper = new JPanel(new BorderLayout(0, 8));
        operatorsWrapper.setBackground(panelBgColor);
        operatorsWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // Add operator button
        JButton addButton = new JButton("+ Add Operator");
        addButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        addButton.setBackground(new Color(60, 120, 90));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> {
            if (operators.size() < 10) {
                addOperator();
            }
        });
        addButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                addButton.setBackground(new Color(70, 140, 105));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                addButton.setBackground(new Color(60, 120, 90));
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(panelBgColor);
        buttonPanel.add(addButton);
        operatorsWrapper.add(buttonPanel, BorderLayout.NORTH);

        // Scrollable operators
        operatorsScroll = new JScrollPane(operatorsPanel);
        operatorsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        operatorsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        operatorsScroll.setBackground(panelBgColor);
        operatorsScroll.setBorder(null);
        operatorsScroll.getViewport().setBackground(panelBgColor);
        operatorsScroll.getVerticalScrollBar().setUnitIncrement(16);
        operatorsWrapper.add(operatorsScroll, BorderLayout.CENTER);

        // ========== RIGHT PANEL (Matrix + Oscilloscope) ==========
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(panelBgColor);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        // Mod matrix
        modMatrixPanel = new ModMatrixPanel(this);

        // Oscilloscope
        oscilloscope = new OscilloscopePanel();
        oscilloscope.setPreferredSize(new Dimension(0, 180));

        rightPanel.add(modMatrixPanel, BorderLayout.CENTER);
        rightPanel.add(oscilloscope, BorderLayout.SOUTH);

        // ========== TOP SPLIT (Operators | Matrix+Scope) ==========
        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, operatorsWrapper, rightPanel);
        topSplit.setDividerLocation(0.55);
        topSplit.setResizeWeight(0.55);
        topSplit.setDividerSize(6);
        topSplit.setBorder(null);
        topSplit.setBackground(bgColor);

        // ========== KEYBOARD (BOTTOM) ==========
        keyboardPanel = new KeyboardPanel(this, synth);
        keyboardPanel.setPreferredSize(new Dimension(0, 140));

        // Add initial operators
        addOperator();
        addOperator();
        addOperator();

        // Set up synthesis
        synth.setOperators(operators);
        modMatrixPanel.updateMatrix(operators);
        synth.setModMatrix(modMatrixPanel.getMatrixValues());

        // Add to frame
        add(topSplit, BorderLayout.CENTER);
        add(keyboardPanel, BorderLayout.SOUTH);

        // Resize handling
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (isResizing) return;
                isResizing = true;

                int height = getHeight();

                // Update keyboard height (proportional but with min/max)
                int keyboardHeight = Math.max(100, Math.min(180, height / 6));
                keyboardPanel.setPreferredSize(new Dimension(0, keyboardHeight));

                // Update oscilloscope height
                int scopeHeight = Math.max(120, Math.min(220, height / 5));
                oscilloscope.setPreferredSize(new Dimension(0, scopeHeight));

                resizeOperatorPanels();
                revalidate();

                isResizing = false;
            }
        });

        // Oscilloscope update timer
        oscilloscopeTimer = new Timer(40, e -> {
            if (oscilloscope != null) {
                oscilloscope.updateSamples(synth.getSamples());
            }
        });
        oscilloscopeTimer.start();

        setVisible(true);

        // Set divider location after visible
        SwingUtilities.invokeLater(() -> topSplit.setDividerLocation(0.55));
    }

    private void resizeOperatorPanels() {
        int availableHeight = operatorsScroll.getViewport().getHeight();
        int availableWidth = operatorsScroll.getViewport().getWidth();

        int numOperators = operatorPanels.size();
        if (numOperators == 0) return;

        int minHeight = (int)(availableHeight * minOperatorPercent);
        int heightPerOperator = availableHeight / numOperators;

        if (heightPerOperator < minHeight) {
            heightPerOperator = minHeight;
        }

        // Apply sizing
        for (OperatorPanel panel : operatorPanels) {
            Dimension size = new Dimension(availableWidth - 10, heightPerOperator);
            panel.setPreferredSize(size);
            panel.setMinimumSize(size);
            panel.setMaximumSize(size);
        }

        int totalHeight = heightPerOperator * numOperators;
        if (totalHeight < availableHeight) {
            heightPerOperator = availableHeight / numOperators;
            for (OperatorPanel panel : operatorPanels) {
                Dimension size = new Dimension(availableWidth - 10, heightPerOperator);
                panel.setPreferredSize(size);
                panel.setMinimumSize(size);
                panel.setMaximumSize(size);
            }
            totalHeight = availableHeight;
        }

        operatorsPanel.setPreferredSize(new Dimension(availableWidth, totalHeight));
        operatorsPanel.revalidate();
        operatorsPanel.repaint();
    }

    private void addOperator() {
        int id = operators.size() + 1;
        Operator op = new Operator(id);
        operators.add(op);

        OperatorPanel panel = new OperatorPanel(op, this);
        operatorPanels.add(panel);
        operatorsPanel.add(panel);

        operatorsPanel.revalidate();
        operatorsPanel.repaint();
        resizeOperatorPanels();

        synth.setOperators(operators);
        modMatrixPanel.updateMatrix(operators);
        synth.setModMatrix(modMatrixPanel.getMatrixValues());
    }

    public void removeOperator(int id) {
        if (operators.size() <= 1) return;

        int indexToRemove = -1;
        for (int i = 0; i < operators.size(); i++) {
            if (operators.get(i).getId() == id) {
                indexToRemove = i;
                break;
            }
        }

        if (indexToRemove == -1) return;

        operators.remove(indexToRemove);
        operatorPanels.remove(indexToRemove);

        // Renumber and rebuild
        operatorsPanel.removeAll();
        operatorPanels.clear();

        for (int i = 0; i < operators.size(); i++) {
            Operator op = operators.get(i);
            op.setId(i + 1);
            OperatorPanel panel = new OperatorPanel(op, this);
            operatorPanels.add(panel);
            operatorsPanel.add(panel);
        }

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
        synth.setOperators(operators);
        if (modMatrixPanel != null) {
            synth.setModMatrix(modMatrixPanel.getMatrixValues());
        }

        for (OperatorPanel opPanel : operatorPanels) {
            opPanel.refreshCanvas();
        }

        if (oscilloscope != null) {
            oscilloscope.updateSamples(synth.getSamples());
        }
    }

    public ArrayList<Operator> getOperators() {
        return operators;
    }
}