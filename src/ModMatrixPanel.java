import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ModMatrixPanel extends JPanel {
    private ArrayList<Operator> operators;
    private JTextField[][] matrixFields;
    private JPanel gridPanel;
    private MainFrame parent;

    public ModMatrixPanel(MainFrame parentFrame) {
        parent = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 50));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(80, 80, 85)),
            BorderFactory.createEmptyBorder(15, 15, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Mod Matrix (Row Modulates Column)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(new Color(200, 200, 210));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(titleLabel, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setBackground(new Color(45, 45, 50));
        add(gridPanel, BorderLayout.CENTER);

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
        gridPanel.removeAll();

        // Create new grid layout (numOps + 1 for labels)
        gridPanel.setLayout(new GridLayout(numOps + 1, numOps + 1, 6, 6));

        // Recreate matrix fields array
        matrixFields = new JTextField[numOps][numOps];

        // Top-left corner (empty)
        JLabel cornerLabel = new JLabel("", SwingConstants.CENTER);
        gridPanel.add(cornerLabel);

        // Column headers (To: Op1, Op2, ...)
        for (int j = 0; j < numOps; j++) {
            JLabel colLabel = new JLabel("Op" + (j + 1), SwingConstants.CENTER);
            colLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            colLabel.setForeground(new Color(180, 180, 190));
            colLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
            gridPanel.add(colLabel);
        }

        // Row headers and matrix cells
        for (int i = 0; i < numOps; i++) {
            // Row header (From: Op1, Op2, ...)
            JLabel rowLabel = new JLabel("Op" + (i + 1), SwingConstants.CENTER);
            rowLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            rowLabel.setForeground(new Color(180, 180, 190));
            rowLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
            gridPanel.add(rowLabel);

            // Matrix cells
            for (int j = 0; j < numOps; j++) {
                // Restore old value if it exists, otherwise use 0
                String initialValue = "0";
                if (oldValues != null && i < oldSize && j < oldSize) {
                    initialValue = String.valueOf(oldValues[i][j]);
                }

                JTextField field = new JTextField(initialValue);
                field.setHorizontalAlignment(JTextField.CENTER);
                field.setFont(new Font("Monospaced", Font.BOLD, 13));
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)
                ));

                // Disable diagonal (operator can't modulate itself)
                if (i == j) {
                    field.setEnabled(false);
                    field.setBackground(new Color(55, 55, 60));
                    field.setForeground(new Color(100, 100, 105));
                } else {
                    field.setBackground(new Color(240, 242, 245));
                    field.setForeground(new Color(30, 30, 35));
                }

                // Add listener to update synthesis when value changes
                field.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        // Value changed, trigger global refresh
                        if (parent != null) {
                            parent.globalRefresh();
                        }
                    }
                });
                field.addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusLost(java.awt.event.FocusEvent e) {
                        // Value changed on focus loss
                        if (parent != null) {
                            parent.globalRefresh();
                        }
                    }
                });

                matrixFields[i][j] = field;
                gridPanel.add(field);
            }
        }

        // Refresh display
        revalidate();
        repaint();
    }

    public int[][] getMatrixValues() {
        int numOps = operators.size();
        int[][] values = new int[numOps][numOps];

        for (int i = 0; i < numOps; i++) {
            for (int j = 0; j < numOps; j++) {
                try {
                    values[i][j] = Integer.parseInt(matrixFields[i][j].getText().trim());
                } catch (NumberFormatException e) {
                    values[i][j] = 0;
                    matrixFields[i][j].setText("0");
                }
            }
        }

        return values;
    }
}
