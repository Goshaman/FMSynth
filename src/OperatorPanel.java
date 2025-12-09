import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class OperatorPanel extends JPanel {
    private Operator operator;
    private MainFrame parent;
    private JTextField functionField;
    private JTextField periodField;
    private JTextField frequencyField;
    private JLabel frequencyLabel;
    private Canvas canvas;

    public OperatorPanel(Operator op, MainFrame pare) {
        operator = op;
        parent = pare;
        // Nicer colors
        Color bgColor = new Color(220, 220, 220);
        Color borderColor = new Color(180, 180, 180);

        setBackground(bgColor);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 3, 3, 3),
                BorderFactory.createCompoundBorder(
                        new LineBorder(borderColor, 1, true),
                        BorderFactory.createEmptyBorder(5, 8, 5, 8)
                )
        ));

        setLayout(new BorderLayout(10, 0));  // 10px gap between controls and canvas

        // Left side - controls
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(bgColor);

        // Right side - canvas
        canvas = new Canvas("Operator");
        canvas.setBackground(Color.WHITE);
        canvas.setBorder(new LineBorder(borderColor, 1));
        add(canvas, BorderLayout.CENTER);

        canvas.setFunction(operator.getFunction(), operator.getPeriod());

        // Top row: label and remove button
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(bgColor);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel nameLabel = new JLabel("Op " + operator.getId());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        topRow.add(nameLabel, BorderLayout.WEST);

        JButton removeButton = new JButton("×");
        removeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        removeButton.setMargin(new Insets(0, 5, 0, 5));
        removeButton.setFocusPainted(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                parent.removeOperator(operator.getId());
            }
        });

        JButton redrawButton = new JButton("↻");
        redrawButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        redrawButton.setMargin(new Insets(0, 5, 0, 5));
        redrawButton.setFocusPainted(false);
        redrawButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateCanvas();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttonPanel.setBackground(bgColor);
        buttonPanel.add(redrawButton);
        buttonPanel.add(removeButton);
        topRow.add(buttonPanel, BorderLayout.EAST);

        controls.add(topRow);
        controls.add(Box.createVerticalStrut(8));

        // Function row
        JPanel funcRow = createFieldRow("Func:", operator.getFunction(), bgColor);
        functionField = (JTextField) funcRow.getComponent(1);
        controls.add(funcRow);
        controls.add(Box.createVerticalStrut(5));
        // Period row
        JPanel perRow = createFieldRow("Per:", operator.getPeriod(), bgColor);
        periodField = (JTextField) perRow.getComponent(1);
        controls.add(perRow);
        controls.add(Box.createVerticalStrut(5));

        // Frequency row
        JPanel freqRow = createFieldRow("Freq:", String.valueOf(operator.getFrequency()), bgColor);
        frequencyLabel = (JLabel) freqRow.getComponent(0);
        frequencyField = (JTextField) freqRow.getComponent(1);
        controls.add(freqRow);

        // Glue pushes everything up
        controls.add(Box.createVerticalGlue());

        // Wrap controls
        JPanel controlsWrapper = new JPanel(new BorderLayout());
        controlsWrapper.setBackground(bgColor);
        controlsWrapper.add(controls, BorderLayout.CENTER);

        add(controlsWrapper, BorderLayout.WEST);

        // Dynamic resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                int panelWidth = getWidth();
                int controlsWidth = (int)(panelWidth * 0.25);  // 25% for controls
                if (controlsWidth < 100) controlsWidth = 100;   // minimum
                if (controlsWidth > 180) controlsWidth = 180;   // maximum
                controlsWrapper.setPreferredSize(new Dimension(controlsWidth, getHeight()));
                revalidate();
            }
        });
    }

    // Helper: creates a row with label and text field side by side
    private JPanel createFieldRow(String labelText, String fieldValue, Color bgColor) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setBackground(bgColor);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setPreferredSize(new Dimension(35, 20));
        row.add(label, BorderLayout.WEST);

        JTextField field = new JTextField(fieldValue);
        field.setFont(new Font("Monospaced", Font.PLAIN, 11));
        row.add(field, BorderLayout.CENTER);

        return row;
    }

    public void setFrequencyVisible(boolean visible) {
        frequencyLabel.setVisible(visible);
        frequencyField.setVisible(visible);
    }

    public Operator getOperator() {
        return operator;
    }

    public void updateCanvas() {
        String func = functionField.getText();
        String per = periodField.getText();

        operator.setFunction(func);
        operator.setPeriod(per);

        canvas.setFunction(func, per);
        parent.updateSynthesis();  // ADD THIS
    }
}