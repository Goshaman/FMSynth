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
        setBackground(Color.GRAY);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20)); // Add padding on right and bottom

        JLabel titleLabel = new JLabel("Mod Matrix (Row Modulates Column)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0)); // Less spacing below title
        add(titleLabel, BorderLayout.NORTH);

        gridPanel = new JPanel();
        gridPanel.setBackground(Color.GRAY);
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
        gridPanel.setLayout(new GridLayout(numOps + 1, numOps + 1, 5, 5));

        // Recreate matrix fields array
        matrixFields = new JTextField[numOps][numOps];

        // Top-left corner (empty)
        JLabel cornerLabel = new JLabel("", SwingConstants.CENTER);
        gridPanel.add(cornerLabel);

        // Column headers (To: Op1, Op2, ...)
        for (int j = 0; j < numOps; j++) {
            JLabel colLabel = new JLabel("Op" + (j + 1), SwingConstants.CENTER);
            colLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
            colLabel.setForeground(Color.WHITE);
            gridPanel.add(colLabel);
        }

        // Row headers and matrix cells
        for (int i = 0; i < numOps; i++) {
            // Row header (From: Op1, Op2, ...)
            JLabel rowLabel = new JLabel("Op" + (i + 1), SwingConstants.CENTER);
            rowLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
            rowLabel.setForeground(Color.WHITE);
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
                field.setFont(new Font("Monospaced", Font.PLAIN, 12));

                // Disable diagonal (operator can't modulate itself)
                if (i == j) {
                    field.setEnabled(false);
                    field.setBackground(Color.DARK_GRAY);
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
