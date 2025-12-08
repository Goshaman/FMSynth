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

    public OperatorPanel(Operator op, MainFrame parent) {
        this.operator = op;
        this.parent = parent;

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
        topRow.add(removeButton, BorderLayout.EAST);

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

        // Right side - canvas
        JPanel canvasArea = new JPanel();
        canvasArea.setBackground(Color.WHITE);
        canvasArea.setBorder(new LineBorder(borderColor, 1));
        add(canvasArea, BorderLayout.CENTER);

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
}