import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class KeyboardPanel extends JPanel {
    private MainFrame parent;
    private Synthesis synth;

    // White key note names (no sharps)
    private String[] whiteNoteNames = {"C", "D", "E", "F", "G", "A", "B"};
    // Which white key indices have a black key to their right (C, D, F, G, A have sharps)
    private boolean[] hasBlackKey = {true, true, false, true, true, true, false};

    private ArrayList<PianoKey> whiteKeys;
    private ArrayList<PianoKey> blackKeys;

    private int startOctave = 2;  // Start from C2
    private int numOctaves = 3;   // 3 octaves + 1 extra note = 22 white keys

    public KeyboardPanel(MainFrame pare, Synthesis synthesis) {
        parent = pare;
        synth = synthesis;

        setBackground(new Color(35, 35, 40));
        setLayout(null);

        whiteKeys = new ArrayList<>();
        blackKeys = new ArrayList<>();

        buildKeyboard();

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateKeyPositions();
            }
        });
    }

    private void buildKeyboard() {
        int whiteKeyIndex = 0;

        // Build 3 full octaves + C of next octave (22 white keys total)
        for (int octave = startOctave; octave <= startOctave + numOctaves; octave++) {
            int notesInOctave = (octave == startOctave + numOctaves) ? 1 : 7; // Last octave only has C

            for (int noteInOctave = 0; noteInOctave < notesInOctave; noteInOctave++) {
                // Create white key
                String whiteName = whiteNoteNames[noteInOctave] + octave;
                int midiNote = getMidiNote(whiteNoteNames[noteInOctave], octave);
                double freq = midiToFreq(midiNote);

                PianoKey whiteKey = new PianoKey(whiteName, freq, false, whiteKeyIndex);
                whiteKeys.add(whiteKey);
                add(whiteKey);

                // Create black key if this white key has one
                if (octave < startOctave + numOctaves && hasBlackKey[noteInOctave]) {
                    String blackName = whiteNoteNames[noteInOctave] + "#" + octave;
                    int blackMidi = midiNote + 1;
                    double blackFreq = midiToFreq(blackMidi);

                    PianoKey blackKey = new PianoKey(blackName, blackFreq, true, whiteKeyIndex);
                    blackKeys.add(blackKey);
                    add(blackKey);
                }

                whiteKeyIndex++;
            }
        }

        // Bring black keys to front
        for (PianoKey blackKey : blackKeys) {
            setComponentZOrder(blackKey, 0);
        }
    }

    private int getMidiNote(String noteName, int octave) {
        // C0 = MIDI 12, so C2 = 36
        int baseNote = 0;
        switch (noteName) {
            case "C": baseNote = 0; break;
            case "D": baseNote = 2; break;
            case "E": baseNote = 4; break;
            case "F": baseNote = 5; break;
            case "G": baseNote = 7; break;
            case "A": baseNote = 9; break;
            case "B": baseNote = 11; break;
        }
        return 12 + (octave * 12) + baseNote;
    }

    private double midiToFreq(int midiNote) {
        return 440.0 * Math.pow(2, (midiNote - 69) / 12.0);
    }

    private void updateKeyPositions() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (panelWidth <= 0 || panelHeight <= 0) return;

        int numWhiteKeys = whiteKeys.size();
        int whiteKeyWidth = panelWidth / numWhiteKeys;
        int whiteKeyHeight = panelHeight;
        int blackKeyWidth = (int)(whiteKeyWidth * 0.65);
        int blackKeyHeight = (int)(panelHeight * 0.62);

        // Position white keys
        for (int i = 0; i < whiteKeys.size(); i++) {
            PianoKey key = whiteKeys.get(i);
            key.setBounds(i * whiteKeyWidth, 0, whiteKeyWidth, whiteKeyHeight);
        }

        // Position black keys
        for (PianoKey blackKey : blackKeys) {
            int whiteIdx = blackKey.getWhiteKeyIndex();
            int xPos = (whiteIdx + 1) * whiteKeyWidth - blackKeyWidth / 2;
            blackKey.setBounds(xPos, 0, blackKeyWidth, blackKeyHeight);
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

    // Inner class for piano keys
    private class PianoKey extends JPanel {
        private String noteName;
        private double frequency;
        private boolean isBlack;
        private int whiteKeyIndex;
        private boolean pressed = false;

        private Color whiteKeyColor = new Color(250, 250, 250);
        private Color whiteKeyPressed = new Color(200, 200, 210);
        private Color blackKeyColor = new Color(30, 30, 35);
        private Color blackKeyPressed = new Color(60, 60, 70);

        public PianoKey(String name, double freq, boolean black, int whiteIdx) {
            noteName = name;
            frequency = freq;
            isBlack = black;
            whiteKeyIndex = whiteIdx;

            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    playNote(frequency);
                    repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    stopNote();
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    if (pressed) {
                        pressed = false;
                        stopNote();
                        repaint();
                    }
                }
            });
        }

        public int getWhiteKeyIndex() {
            return whiteKeyIndex;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isBlack) {
                // Black key with gradient
                Color baseColor = pressed ? blackKeyPressed : blackKeyColor;
                GradientPaint gradient = new GradientPaint(0, 0, baseColor.brighter(), 0, h, baseColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(1, 0, w - 2, h - 4, 4, 4);

                // Bottom shadow
                g2d.setColor(new Color(15, 15, 20));
                g2d.fillRoundRect(1, h - 8, w - 2, 6, 3, 3);

                // Label
                g2d.setColor(new Color(180, 180, 190));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
                FontMetrics fm = g2d.getFontMetrics();
                String label = noteName.substring(0, noteName.length() - 1);  // Show note with # but without octave
                int textX = (w - fm.stringWidth(label)) / 2;
                g2d.drawString(label, textX, h - 15);
            } else {
                // White key with gradient
                Color baseColor = pressed ? whiteKeyPressed : whiteKeyColor;
                GradientPaint gradient = new GradientPaint(0, 0, baseColor, 0, h, new Color(230, 230, 235));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(1, 0, w - 2, h - 3, 3, 3);

                // Border
                g2d.setColor(new Color(180, 180, 185));
                g2d.drawRoundRect(1, 0, w - 3, h - 3, 3, 3);

                // Bottom edge shadow
                g2d.setColor(new Color(200, 200, 205));
                g2d.fillRect(1, h - 6, w - 2, 4);

                // Label at bottom
                g2d.setColor(new Color(100, 100, 110));
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (w - fm.stringWidth(noteName)) / 2;
                g2d.drawString(noteName, textX, h - 10);
            }
        }
    }
}