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
        // Dark mode colors
        Color bgColor = new Color(55, 55, 60);
        Color borderColor = new Color(80, 80, 85);

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
        canvas.setBackground(new Color(30, 30, 35));
        canvas.setBorder(new LineBorder(borderColor, 1));
        add(canvas, BorderLayout.CENTER);

        canvas.setFunction(operator.getFunction(), operator.getPeriod());

        // Top row: label, carrier/modulator toggle, and remove button
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(bgColor);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setBackground(bgColor);

        JLabel nameLabel = new JLabel("Op " + operator.getId());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLabel.setForeground(new Color(200, 200, 210));
        leftPanel.add(nameLabel);

        // Carrier/Modulator toggle button
        final JToggleButton typeToggle = new JToggleButton(operator.isCarrier() ? "C" : "M");
        typeToggle.setSelected(operator.isCarrier());
        typeToggle.setFont(new Font("SansSerif", Font.BOLD, 10));
        typeToggle.setMargin(new Insets(0, 8, 0, 8));
        typeToggle.setFocusPainted(false);
        typeToggle.setBackground(new Color(70, 70, 75));
        typeToggle.setForeground(new Color(200, 200, 210));
        typeToggle.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 95), 1));
        typeToggle.setToolTipText(operator.isCarrier() ? "Carrier" : "Modulator");
        typeToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean isCarrier = typeToggle.isSelected();
                operator.setCarrier(isCarrier);
                typeToggle.setText(isCarrier ? "C" : "M");
                typeToggle.setToolTipText(isCarrier ? "Carrier" : "Modulator");
                // Show frequency field only for modulators
                setFrequencyVisible(!isCarrier);
                parent.globalRefresh();
            }
        });
        leftPanel.add(typeToggle);

        topRow.add(leftPanel, BorderLayout.WEST);

        JButton removeButton = new JButton("×");
        removeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        removeButton.setMargin(new Insets(0, 5, 0, 5));
        removeButton.setFocusPainted(false);
        removeButton.setBackground(new Color(70, 70, 75));
        removeButton.setForeground(new Color(200, 200, 210));
        removeButton.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 95), 1));
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
        functionField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateCanvas();
            }
        });
        functionField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                updateCanvas();
            }
        });
        controls.add(funcRow);
        controls.add(Box.createVerticalStrut(5));

        // Period row
        JPanel perRow = createFieldRow("Per:", operator.getPeriod(), bgColor);
        periodField = (JTextField) perRow.getComponent(1);
        periodField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateCanvas();
            }
        });
        periodField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                updateCanvas();
            }
        });
        controls.add(perRow);
        controls.add(Box.createVerticalStrut(5));

        // Frequency row
        JPanel freqRow = createFieldRow("Freq:", String.valueOf(operator.getFrequency()), bgColor);
        frequencyLabel = (JLabel) freqRow.getComponent(0);
        frequencyField = (JTextField) freqRow.getComponent(1);
        frequencyField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateFrequency();
            }
        });
        frequencyField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) {
                updateFrequency();
            }
        });
        controls.add(freqRow);

        // Initially hide frequency for carriers (they use keyboard frequency)
        setFrequencyVisible(!operator.isCarrier());

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
        label.setForeground(new Color(180, 180, 190));
        label.setPreferredSize(new Dimension(35, 20));
        row.add(label, BorderLayout.WEST);

        JTextField field = new JTextField(fieldValue);
        field.setFont(new Font("Monospaced", Font.PLAIN, 11));
        field.setBackground(new Color(240, 242, 245));
        field.setForeground(new Color(30, 30, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));
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
        parent.globalRefresh();
    }

    public void refreshCanvas() {
        // Refresh canvas without changing operator values
        canvas.setFunction(operator.getFunction(), operator.getPeriod());
    }

    private void updateFrequency() {
        try {
            double freq = Double.parseDouble(frequencyField.getText());
            operator.setFrequency(freq);
            parent.globalRefresh();
        } catch (NumberFormatException e) {
            // Reset to current value if invalid
            frequencyField.setText(String.valueOf(operator.getFrequency()));
        }
    }
}