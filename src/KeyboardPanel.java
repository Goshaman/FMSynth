import javax.swing.*;
import java.awt.*;

public class KeyboardPanel extends JPanel {
    private MainFrame parent;
    private Synthesis synth;
    // Note names for one octave
    private String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public KeyboardPanel(MainFrame pare, Synthesis synthesis) {
        parent = pare;
        synth = synthesis;

        setBackground(new Color(50, 50, 50));
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 5));

        buildKeyboard();
    }

    private void buildKeyboard() {
        setLayout(null); // Use absolute positioning for piano layout

        // 37 keys = 3 octaves + 1 key (C3 to C6)
        int whiteKeyCount = 0;
        int whiteKeyWidth = 40;
        int whiteKeyHeight = 100;
        int blackKeyWidth = 24;
        int blackKeyHeight = 65;

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
                key.setBounds(whiteKeyCount * whiteKeyWidth - blackKeyWidth/2, 0,
                        blackKeyWidth, blackKeyHeight);
            } else {
                // White key
                key.setBackground(Color.WHITE);
                key.setForeground(Color.BLACK);
                key.setBounds(whiteKeyCount * whiteKeyWidth, 0,
                        whiteKeyWidth, whiteKeyHeight);
                whiteKeyCount++;
            }

            key.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

            key.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent e) {
                    playNote(keyFreq);
                }
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    stopNote();
                }
            });

            // Add black keys last so they appear on top
            if (isBlack) {
                add(key, 0); // Add to front
            } else {
                add(key);
            }
        }

        // Set preferred size based on white keys
        setPreferredSize(new Dimension(whiteKeyCount * whiteKeyWidth, whiteKeyHeight));
    }

    private void playNote(double freq) {
        synth.setFrequency(freq);
        synth.playSignal();
    }

    private void stopNote() {
        synth.stopSignal();
    }
}