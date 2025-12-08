import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import org.mariuszgromada.math.mxparser.*;
import java.util.HashMap;

public class MainFrame extends JFrame {
    private static final int NUM_OPERATORS = 6;

    private Synthesis synth;
    private HashMap<String, JTextField> textFields;
    private HashMap<Integer, Canvas> operatorCanvases;
    private Canvas outputCanvas;

    public MainFrame(Synthesis s) {
        synth = s;
        textFields = new HashMap<>();
        operatorCanvases = new HashMap<>();

        setTitle("FMSynth - 6 Operators");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Main container with padding
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top section: 6 operators side by side
        JPanel operatorsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        operatorsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            "Operators",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));

        for (int i = 0; i < NUM_OPERATORS; i++) {
            operatorsPanel.add(createOperatorPanel(i));
        }

        // Middle section: Modulation Matrix
        JPanel matrixPanel = createModulationMatrixPanel();

        // Bottom section: Output and controls
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton redrawBtn = new JButton("Update & Redraw");
        redrawBtn.setFont(new Font("Arial", Font.BOLD, 12));
        redrawBtn.setBackground(new Color(100, 200, 100));
        redrawBtn.setForeground(Color.BLACK);
        redrawBtn.setFocusPainted(false);
        redrawBtn.setPreferredSize(new Dimension(150, 35));
        redrawBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        redrawBtn.addActionListener(e -> remake());

        JButton playBtn = new JButton("Play (1s)");
        playBtn.setFont(new Font("Arial", Font.BOLD, 12));
        playBtn.setBackground(new Color(100, 150, 255));
        playBtn.setForeground(Color.WHITE);
        playBtn.setFocusPainted(false);
        playBtn.setPreferredSize(new Dimension(150, 35));
        playBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        playBtn.addActionListener(e -> {
            try {
                synth.playSignal();
                Thread.sleep(1000);
                synth.stopSignal();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        controlPanel.add(redrawBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(playBtn);

        // Output visualization panel
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            "Output Waveform",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));

        outputCanvas = new Canvas("Output");
        outputCanvas.setPreferredSize(new Dimension(800, 200));
        outputCanvas.setBackground(Color.WHITE);
        outputPanel.add(outputCanvas, BorderLayout.CENTER);

        bottomPanel.add(controlPanel, BorderLayout.WEST);
        bottomPanel.add(outputPanel, BorderLayout.CENTER);

        // Add all sections to main container
        mainContainer.add(operatorsPanel, BorderLayout.NORTH);
        mainContainer.add(matrixPanel, BorderLayout.CENTER);
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        setSize(1400, 900);
        setVisible(true);
    }

    private JPanel createOperatorPanel(int opNum) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            "Op " + opNum,
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 11)
        ));
        panel.setBackground(new Color(240, 240, 240));

        // Canvas for waveform visualization
        Canvas canvas = new Canvas("Op" + opNum);
        canvas.setPreferredSize(new Dimension(200, 120));
        canvas.setBackground(Color.WHITE);
        canvas.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        operatorCanvases.put(opNum, canvas);

        // Input fields panel
        JPanel inputsPanel = new JPanel();
        inputsPanel.setLayout(new BoxLayout(inputsPanel, BoxLayout.Y_AXIS));
        inputsPanel.setBackground(new Color(240, 240, 240));
        inputsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Function
        JPanel funcPanel = new JPanel(new BorderLayout(3, 0));
        funcPanel.setBackground(new Color(240, 240, 240));
        JLabel funcLabel = new JLabel("f(t):");
        funcLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        funcLabel.setPreferredSize(new Dimension(30, 20));
        JTextField funcField = createTextField("Op" + opNum + "_function", "sin(t)");
        funcPanel.add(funcLabel, BorderLayout.WEST);
        funcPanel.add(funcField, BorderLayout.CENTER);
        funcPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        // Period
        JPanel periodPanel = new JPanel(new BorderLayout(3, 0));
        periodPanel.setBackground(new Color(240, 240, 240));
        JLabel periodLabel = new JLabel("T:");
        periodLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        periodLabel.setPreferredSize(new Dimension(30, 20));
        JTextField periodField = createTextField("Op" + opNum + "_period", "2*pi");
        periodPanel.add(periodLabel, BorderLayout.WEST);
        periodPanel.add(periodField, BorderLayout.CENTER);
        periodPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        // Frequency
        JPanel freqPanel = new JPanel(new BorderLayout(3, 0));
        freqPanel.setBackground(new Color(240, 240, 240));
        JLabel freqLabel = new JLabel("Hz:");
        freqLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        freqLabel.setPreferredSize(new Dimension(30, 20));
        JTextField freqField = createTextField("Op" + opNum + "_freq", "440.0");
        freqPanel.add(freqLabel, BorderLayout.WEST);
        freqPanel.add(freqField, BorderLayout.CENTER);
        freqPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        inputsPanel.add(funcPanel);
        inputsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        inputsPanel.add(periodPanel);
        inputsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        inputsPanel.add(freqPanel);

        panel.add(canvas, BorderLayout.NORTH);
        panel.add(inputsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createModulationMatrixPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            "Modulation Matrix (Row → Column)",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        panel.setBackground(Color.WHITE);

        JPanel gridPanel = new JPanel(new GridLayout(NUM_OPERATORS + 1, NUM_OPERATORS + 1, 3, 3));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridPanel.setBackground(Color.WHITE);

        // Header row
        gridPanel.add(createMatrixLabel("", true));
        for (int i = 0; i < NUM_OPERATORS; i++) {
            gridPanel.add(createMatrixLabel("Op" + i, true));
        }

        // Data rows
        for (int from = 0; from < NUM_OPERATORS; from++) {
            gridPanel.add(createMatrixLabel("Op" + from, true));
            for (int to = 0; to < NUM_OPERATORS; to++) {
                JTextField field = createTextField("mod_" + from + "_" + to, "0");
                field.setFont(new Font("Monospaced", Font.PLAIN, 11));
                field.setHorizontalAlignment(JTextField.CENTER);
                if (from == to) {
                    field.setBackground(new Color(220, 220, 220));
                    field.setEnabled(false);
                }
                gridPanel.add(field);
            }
        }

        panel.add(gridPanel, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("Enter modulation strength values. Example: Op1→Op0 = 100 means Op1 modulates Op0 with strength 100");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createMatrixLabel(String text, boolean bold) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        if (bold) {
            label.setFont(new Font("Arial", Font.BOLD, 11));
        } else {
            label.setFont(new Font("Arial", Font.PLAIN, 11));
        }
        return label;
    }

    private JTextField createTextField(String name, String defaultValue) {
        JTextField field = new JTextField(defaultValue);
        field.setName(name);
        field.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textFields.put(name, field);
        return field;
    }

    private void remake() {
        try {
            String[] operatorFunctions = new String[NUM_OPERATORS];
            String[] periods = new String[NUM_OPERATORS];
            double[] frequencies = new double[NUM_OPERATORS];
            double[][] modulationMatrix = new double[NUM_OPERATORS][NUM_OPERATORS];

            // Read operator parameters
            for (int i = 0; i < NUM_OPERATORS; i++) {
                JTextField funcField = textFields.get("Op" + i + "_function");
                JTextField periodField = textFields.get("Op" + i + "_period");
                JTextField freqField = textFields.get("Op" + i + "_freq");

                operatorFunctions[i] = funcField.getText().trim().isEmpty() ? "sin(t)" : funcField.getText().trim();
                periods[i] = periodField.getText().trim().isEmpty() ? "2*pi" : periodField.getText().trim();

                try {
                    frequencies[i] = Double.parseDouble(freqField.getText().trim());
                } catch (NumberFormatException e) {
                    frequencies[i] = 440.0;
                    freqField.setText("440.0");
                }
            }

            // Read modulation matrix
            for (int from = 0; from < NUM_OPERATORS; from++) {
                for (int to = 0; to < NUM_OPERATORS; to++) {
                    JTextField modField = textFields.get("mod_" + from + "_" + to);
                    try {
                        modulationMatrix[from][to] = modField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(modField.getText().trim());
                    } catch (NumberFormatException e) {
                        modulationMatrix[from][to] = 0.0;
                        modField.setText("0");
                    }
                }
            }

            // Update synthesis parameters
            Params params = new Params(operatorFunctions, periods, frequencies, modulationMatrix);
            System.out.println(params);
            synth.updateParams(params);

            // Update operator canvases
            for (int i = 0; i < NUM_OPERATORS; i++) {
                Canvas canvas = operatorCanvases.get(i);
                if (canvas != null) {
                    canvas.changeParam(synth, periods[i], frequencies[i]);
                }
            }

            // Update output canvas
            if (outputCanvas != null) {
                outputCanvas.changeParam(synth.getSamples(), periods[0], 200);
            }

            // Repaint all canvases
            for (Canvas canvas : operatorCanvases.values()) {
                canvas.repaint();
            }
            outputCanvas.repaint();

            System.out.println("Parameters updated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error updating parameters: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
