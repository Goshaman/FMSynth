import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ModMatrixPanel extends JPanel {
    private ArrayList<Operator> operators;
    private JTextField[][] matrixFields;
    private JPanel gridPanel;
    private JPanel gridWrapper;
    private MainFrame parent;

    // Colors
    private Color bgColor = new Color(45, 47, 52);
    private Color headerBgColor = new Color(55, 57, 62);
    private Color cellBgColor = new Color(240, 242, 245);
    private Color cellDisabledBg = new Color(50, 52, 57);
    private Color borderColor = new Color(65, 67, 72);
    private Color textColor = new Color(210, 212, 218);
    private Color mutedTextColor = new Color(140, 142, 148);
    private Color accentColor = new Color(100, 180, 255);

    public ModMatrixPanel(MainFrame parentFrame) {
        parent = parentFrame;
        setLayout(new BorderLayout(0, 15));
        setBackground(bgColor);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, borderColor),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(bgColor);
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("Modulation Matrix");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Rows modulate columns \u2022 Values: 0-10");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitleLabel.setForeground(mutedTextColor);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitleLabel);

        add(titlePanel, BorderLayout.NORTH);

        // Grid wrapper to center the matrix
        gridWrapper = new JPanel(new GridBagLayout());
        gridWrapper.setBackground(bgColor);

        gridPanel = new JPanel();
        gridPanel.setBackground(bgColor);

        gridWrapper.add(gridPanel);
        add(gridWrapper, BorderLayout.CENTER);

        operators = new ArrayList<>();
        matrixFields = new JTextField[0][0];
    }

    public void updateMatrix(ArrayList<Operator> ops) {
        operators = ops;
        int numOps = operators.size();

        // Save old matrix values
        int[][] oldValues = null;
        int oldSize = 0;
        if (matrixFields != null && matrixFields.length > 0) {
            oldSize = matrixFields.length;
            oldValues = new int[oldSize][oldSize];
            for (int i = 0; i < oldSize; i++) {
                for (int j = 0; j < oldSize; j++) {
                    try {
                        oldValues[i][j] = Integer.parseInt(matrixFields[i][j].getText().trim());
                    } catch (NumberFormatException e) {
                        oldValues[i][j] = 0;
                    }
                }
            }
        }

        // Clear existing grid
        // Clear existing grid
        // Clear existing grid
        // Clear existing grid
        gridPanel.removeAll();

        int gap = 5;
        int availableSize = Math.min(gridWrapper.getWidth(), gridWrapper.getHeight());
        if (availableSize < 100) availableSize = 400;  // default before first render
        int totalMatrixSize = (int)(availableSize * 0.85);  // 85% of container
        int numCells = numOps + 1;
        int cellSize = (totalMatrixSize - (gap * (numCells - 1))) / numCells;

// Create new grid layout
        gridPanel.setLayout(new GridLayout(numOps + 1, numOps + 1, gap, gap));
        gridPanel.setPreferredSize(new Dimension(totalMatrixSize, totalMatrixSize));
        matrixFields = new JTextField[numOps][numOps];

        // Top-left corner (arrow indicator)
        JPanel cornerPanel = createHeaderCell("\u2193 mod \u2192", true, numOps);
        gridPanel.add(cornerPanel);

        // Column headers (destinations)
        for (int j = 0; j < numOps; j++) {
            String label = String.valueOf(j + 1);
            JPanel headerPanel = createHeaderCell(label, false, numOps);
            // Color code based on carrier/modulator
            if (operators.get(j).isCarrier()) {
                headerPanel.setBackground(new Color(60, 80, 70));
            } else {
                headerPanel.setBackground(new Color(80, 65, 55));
            }
            gridPanel.add(headerPanel);
        }

        // Row headers and matrix cells
        for (int i = 0; i < numOps; i++) {
            // Row header (source)
            String label = String.valueOf(i + 1);
            JPanel rowHeader = createHeaderCell(label, false, numOps);
            // Color code based on carrier/modulator
            if (operators.get(i).isCarrier()) {
                rowHeader.setBackground(new Color(60, 80, 70));
            } else {
                rowHeader.setBackground(new Color(80, 65, 55));
            }
            gridPanel.add(rowHeader);

            // Matrix cells
            for (int j = 0; j < numOps; j++) {
                String initialValue = "0";
                if (oldValues != null && i < oldSize && j < oldSize) {
                    initialValue = String.valueOf(oldValues[i][j]);
                }

                JTextField field = new JTextField(initialValue);
                field.setHorizontalAlignment(JTextField.CENTER);
                // Scale font based on cell size
                int fontSize = Math.max(10, 18 - numOps);
                field.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                field.setPreferredSize(new Dimension(cellSize, cellSize));
                field.setMinimumSize(new Dimension(cellSize, cellSize));

                // Disable diagonal (can't modulate itself)
                if (i == j) {
                    field.setEnabled(false);
                    field.setBackground(cellDisabledBg);
                    field.setForeground(new Color(80, 82, 87));
                    field.setText("-");
                    field.setBorder(BorderFactory.createLineBorder(new Color(55, 57, 62), 1));
                } else {
                    field.setBackground(cellBgColor);
                    field.setForeground(new Color(30, 32, 38));
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borderColor, 1),
                            BorderFactory.createEmptyBorder(2, 2, 2, 2)
                    ));

                    // Focus highlighting
                    field.addFocusListener(new java.awt.event.FocusAdapter() {
                        public void focusGained(java.awt.event.FocusEvent e) {
                            field.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(accentColor, 2),
                                    BorderFactory.createEmptyBorder(1, 1, 1, 1)
                            ));
                            field.selectAll();
                        }
                        public void focusLost(java.awt.event.FocusEvent e) {
                            field.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(borderColor, 1),
                                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
                            ));
                            validateAndUpdate(field);
                        }
                    });

                    field.addActionListener(e -> validateAndUpdate(field));
                }

                matrixFields[i][j] = field;
                gridPanel.add(field);
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createHeaderCell(String text, boolean isCorner, int numOps) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(headerBgColor);
        panel.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        int fontSize = Math.max(9, 16 - numOps);
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", isCorner ? Font.PLAIN : Font.BOLD, isCorner ? Math.max(8, fontSize - 2) : fontSize));
        label.setForeground(isCorner ? mutedTextColor : textColor);

        panel.add(label);
        return panel;
    }

    private void validateAndUpdate(JTextField field) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            // Clamp to 0-10
            value = Math.max(0, Math.min(10, value));
            field.setText(String.valueOf(value));
        } catch (NumberFormatException e) {
            field.setText("0");
        }

        if (parent != null) {
            parent.globalRefresh();
        }
    }

    public int[][] getMatrixValues() {
        int numOps = operators.size();
        int[][] values = new int[numOps][numOps];

        for (int i = 0; i < numOps; i++) {
            for (int j = 0; j < numOps; j++) {
                if (i == j) {
                    values[i][j] = 0;
                } else {
                    try {
                        values[i][j] = Integer.parseInt(matrixFields[i][j].getText().trim());
                    } catch (NumberFormatException e) {
                        values[i][j] = 0;
                        matrixFields[i][j].setText("0");
                    }
                }
            }
        }

        return values;
    }
}