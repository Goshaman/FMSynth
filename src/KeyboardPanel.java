import javax.swing.*;
import java.awt.*;

public class KeyboardPanel extends JPanel {
    private MainFrame parent;
    private Synthesis synth;
    // Note names for one octave
    private String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private java.util.ArrayList<JButton> whiteKeys;
    private java.util.ArrayList<JButton> blackKeys;
    private int whiteKeyCount = 22; // 37 keys = 22 white keys

    public KeyboardPanel(MainFrame pare, Synthesis synthesis) {
        parent = pare;
        synth = synthesis;

        setBackground(new Color(50, 50, 50));
        setLayout(null); // Use null layout for absolute positioning

        whiteKeys = new java.util.ArrayList<>();
        blackKeys = new java.util.ArrayList<>();

        buildKeyboard();

        // Add resize listener to update key positions
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateKeyPositions();
            }
        });
    }

    private void buildKeyboard() {
        int currentWhiteKey = 0;

        for (int i = 0; i < 37; i++) {
            int noteIndex = i % 12;
            int octave = 3 + (i / 12);
            String noteName = noteNames[noteIndex] + octave;

            // Calculate frequency
            int midiNote = 48 + i; // C3 = 48
            double freq = 440.0 * Math.pow(2, (midiNote - 69) / 12.0);
            final double keyFreq = freq;

            JButton key = new JButton(noteName);
            key.setFont(new Font("SansSerif", Font.PLAIN, 9));
            key.setFocusPainted(false);

            boolean isBlack = noteNames[noteIndex].contains("#");

            if (isBlack) {
                // Black key
                key.setBackground(Color.BLACK);
                key.setForeground(Color.WHITE);
                blackKeys.add(key);
            } else {
                // White key
                key.setBackground(Color.WHITE);
                key.setForeground(Color.BLACK);
                whiteKeys.add(key);
                currentWhiteKey++;
            }

            key.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

            // Store white key index for black key positioning
            final int whiteKeyIdx = currentWhiteKey;
            final boolean isBlackKey = isBlack;

            key.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent e) {
                    playNote(keyFreq);
                }
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    stopNote();
                }
            });

            add(key);
        }

        // Move black keys to front (on top)
        for (JButton blackKey : blackKeys) {
            remove(blackKey);
            add(blackKey);
        }
    }

    private void updateKeyPositions() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth <= 0 || panelHeight <= 0) return;

        // Calculate key dimensions based on panel size
        int whiteKeyWidth = panelWidth / whiteKeyCount;
        int whiteKeyHeight = panelHeight;
        int blackKeyWidth = (int)(whiteKeyWidth * 0.6);
        int blackKeyHeight = (int)(panelHeight * 0.65);

        // Position white keys
        for (int i = 0; i < whiteKeys.size(); i++) {
            JButton key = whiteKeys.get(i);
            key.setBounds(i * whiteKeyWidth, 0, whiteKeyWidth, whiteKeyHeight);
        }

        // Position black keys (between white keys)
        int whiteKeyIdx = 0;
        for (int i = 0; i < 37; i++) {
            int noteIndex = i % 12;
            boolean isBlack = noteNames[noteIndex].contains("#");

            if (isBlack) {
                // Find corresponding black key in list
                int blackKeyIdx = 0;
                for (int j = 0; j < i; j++) {
                    if (noteNames[j % 12].contains("#")) {
                        blackKeyIdx++;
                    }
                }

                if (blackKeyIdx < blackKeys.size()) {
                    JButton blackKey = blackKeys.get(blackKeyIdx);
                    int xPos = (whiteKeyIdx * whiteKeyWidth) + whiteKeyWidth - (blackKeyWidth / 2);
                    blackKey.setBounds(xPos, 0, blackKeyWidth, blackKeyHeight);
                }
            } else {
                whiteKeyIdx++;
            }
        }

        revalidate();
        repaint();
    }

    private void playNote(double freq) {
        synth.setFrequency(freq);
        synth.playSignal();
    }

    private void stopNote() {
        synth.stopSignal();
    }
}