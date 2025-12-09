import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class OperatorPanel extends JPanel {
    private Operator operator;
    private MainFrame parent;
    private JTextField functionField;
    private JTextField periodField;
    private JTextField frequencyField;
    private JPanel frequencyRow;
    private Canvas canvas;
    private JLabel nameLabel;
    private JToggleButton typeToggle;

    // Colors
    private Color bgColor = new Color(50, 52, 58);
    private Color borderColor = new Color(70, 72, 78);
    private Color accentColor = new Color(100, 180, 255);
    private Color textColor = new Color(210, 212, 218);
    private Color mutedTextColor = new Color(140, 142, 148);
    private Color fieldBgColor = new Color(38, 40, 46);
    private Color carrierColor = new Color(100, 200, 150);
    private Color modulatorColor = new Color(255, 150, 100);

    public OperatorPanel(Operator op, MainFrame pare) {
        operator = op;
        parent = pare;

        setBackground(bgColor);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4),
                BorderFactory.createCompoundBorder(
                        new LineBorder(borderColor, 1, true),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                )
        ));

        setLayout(new BorderLayout(15, 0));

        // ========== LEFT SIDE - CONTROLS ==========
        JPanel controlsWrapper = new JPanel(new BorderLayout());
        controlsWrapper.setBackground(bgColor);
        controlsWrapper.setPreferredSize(new Dimension(160, 0));

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(bgColor);

        // Header row: Op name + type toggle + remove button
        JPanel headerRow = new JPanel(new BorderLayout(8, 0));
        headerRow.setBackground(bgColor);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // Left part: name and type
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftHeader.setBackground(bgColor);

        nameLabel = new JLabel("Op " + operator.getId());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLabel.setForeground(textColor);
        leftHeader.add(nameLabel);

        // Type toggle (Carrier/Modulator)
        typeToggle = new JToggleButton(operator.isCarrier() ? "C" : "M");
        typeToggle.setSelected(operator.isCarrier());
        typeToggle.setFont(new Font("SansSerif", Font.BOLD, 10));
        typeToggle.setPreferredSize(new Dimension(28, 22));
        typeToggle.setFocusPainted(false);
        typeToggle.setBackground(operator.isCarrier() ? carrierColor : modulatorColor);
        typeToggle.setForeground(new Color(30, 30, 35));
        typeToggle.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        typeToggle.setToolTipText(operator.isCarrier() ? "Carrier (plays note)" : "Modulator (modifies carriers)");
        typeToggle.addActionListener(e -> {
            boolean isCarrier = typeToggle.isSelected();
            operator.setCarrier(isCarrier);
            typeToggle.setText(isCarrier ? "C" : "M");
            typeToggle.setBackground(isCarrier ? carrierColor : modulatorColor);
            typeToggle.setToolTipText(isCarrier ? "Carrier (plays note)" : "Modulator (modifies carriers)");
            setFrequencyVisible(!isCarrier);
            parent.globalRefresh();
        });
        leftHeader.add(typeToggle);

        headerRow.add(leftHeader, BorderLayout.WEST);

        // Remove button
        JButton removeButton = new JButton("\u00D7"); // Unicode multiplication sign
        removeButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        removeButton.setPreferredSize(new Dimension(26, 22));
        removeButton.setFocusPainted(false);
        removeButton.setBackground(new Color(80, 60, 60));
        removeButton.setForeground(new Color(220, 150, 150));
        removeButton.setBorder(BorderFactory.createEmptyBorder());
        removeButton.setToolTipText("Remove operator");
        removeButton.addActionListener(e -> parent.removeOperator(operator.getId()));
        removeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                removeButton.setBackground(new Color(120, 60, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                removeButton.setBackground(new Color(80, 60, 60));
            }
        });

        headerRow.add(removeButton, BorderLayout.EAST);

        controls.add(headerRow);
        controls.add(Box.createVerticalStrut(12));

        // Function field
        JPanel funcRow = createFieldRow("f(t)", operator.getFunction());
        functionField = getFieldFromRow(funcRow);
        functionField.addActionListener(e -> {
            updateCanvas();
            canvas.repaint();
        });
        functionField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { updateCanvas(); }
        });
        controls.add(funcRow);
        controls.add(Box.createVerticalStrut(8));

        // Period field
        JPanel perRow = createFieldRow("Period", operator.getPeriod());
        periodField = getFieldFromRow(perRow);
        periodField.addActionListener(e -> {
            updateCanvas();
            canvas.repaint();
        });
        periodField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { updateCanvas(); }
        });
        controls.add(perRow);
        controls.add(Box.createVerticalStrut(8));

        // Frequency field (only shown for modulators)
        frequencyRow = createFieldRow("Freq", String.valueOf((int)operator.getFrequency()) + " Hz");
        frequencyField = getFieldFromRow(frequencyRow);
        frequencyField.addActionListener(e -> updateFrequency());
        frequencyField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { updateFrequency(); }
        });
        controls.add(frequencyRow);

        // Hide frequency for carriers (they use keyboard frequency)
        setFrequencyVisible(!operator.isCarrier());

        controls.add(Box.createVerticalGlue());

        controlsWrapper.add(controls, BorderLayout.NORTH);
        add(controlsWrapper, BorderLayout.WEST);

        // ========== RIGHT SIDE - CANVAS ==========
        canvas = new Canvas("Operator");
        canvas.setBackground(new Color(28, 30, 36));
        canvas.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        canvas.setFunction(operator.getFunction(), operator.getPeriod());
        add(canvas, BorderLayout.CENTER);
    }

    private JPanel createFieldRow(String labelText, String fieldValue) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(bgColor);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(mutedTextColor);
        label.setPreferredSize(new Dimension(42, 22));
        row.add(label, BorderLayout.WEST);

        JTextField field = new JTextField(fieldValue);
        field.setFont(new Font("Monospaced", Font.PLAIN, 11));
        field.setBackground(fieldBgColor);
        field.setForeground(textColor);
        field.setCaretColor(accentColor);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accentColor, 1),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1),
                        BorderFactory.createEmptyBorder(3, 6, 3, 6)
                ));
            }
        });
        row.add(field, BorderLayout.CENTER);

        return row;
    }

    private JTextField getFieldFromRow(JPanel row) {
        for (Component c : row.getComponents()) {
            if (c instanceof JTextField) {
                return (JTextField) c;
            }
        }
        return null;
    }

    public void setFrequencyVisible(boolean visible) {
        frequencyRow.setVisible(visible);
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
        canvas.setFunction(operator.getFunction(), operator.getPeriod());
    }

    private void updateFrequency() {
        try {
            String text = frequencyField.getText().replace("Hz", "").trim();
            double freq = Double.parseDouble(text);
            operator.setFrequency(freq);
            frequencyField.setText((int)freq + " Hz");
            parent.globalRefresh();
        } catch (NumberFormatException e) {
            frequencyField.setText((int)operator.getFrequency() + " Hz");
        }
    }

    public void updateNameLabel() {
        nameLabel.setText("Op " + operator.getId());
    }
}