package com.steganography.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.geom.*;

public class DecodePanel extends JPanel {

    private static final Color BLUE = new Color(0x1172E4);
    private static final Color BLACK = Color.BLACK;
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_TAB = new Color(0xDDDDDD);
    private static final Color GRAY_HINT = new Color(0x676767);
    private static final Color RADIO_BORDER = new Color(0xD9D9D9);

    private JButton decodeBtn;
    private JRadioButton radioText, radioFile;
    private Runnable resizeCallback;
    private Runnable onDecode;

    private final int W = 860;

    public DecodePanel() {
        setLayout(null);
        setBackground(WHITE);
        buildAll();
    }

    public void setResizeCallback(Runnable cb) { this.resizeCallback = cb; }
    public void setOnDecode(Runnable cb) { this.onDecode = cb; }
    public boolean isResultText() { return radioText != null && radioText.isSelected(); }

    private void buildAll() {
        int y = addDecodeContent(0, 0, W);
        setPreferredSize(new Dimension(W, y + 40));
    }

    private int addDecodeContent(int x, int y, int w) {
        y = addLabel("Select an AVI Stego-Video to upload", BLACK, Font.PLAIN, 20, x, y, w);
        y += 10;

        JButton chooseStegoBtn = makeBlueButtonRaw("Choose File", 241, 42);
        chooseStegoBtn.setBounds(x, y, 241, 42);
        add(chooseStegoBtn);

        JLabel stegoFileLabel = makeHintLabel("", x + 251, y + 11, w - 251);
        add(stegoFileLabel);

        chooseStegoBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("AVI Video Files (*.avi)", "avi"));
            fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(DecodePanel.this) == JFileChooser.APPROVE_OPTION)
                stegoFileLabel.setText(fc.getSelectedFile().getName());
        });

        y += 42 + 30;

        y = addLabel("Expected output type", BLACK, Font.PLAIN, 20, x, y, w);
        y += 10;

        ButtonGroup outputGroup = new ButtonGroup();
        radioText = makeRadioButton("Text", outputGroup);
        radioFile = makeRadioButton("File", outputGroup);
        radioText.setSelected(true);

        radioText.setBounds(x, y, 120, 26);
        add(radioText);
        y += 36;

        radioFile.setBounds(x, y, 120, 26);
        add(radioFile);
        y += 26 + 30;

        y = addLabel("Enter password (optional)", BLACK, Font.PLAIN, 20, x, y, w);
        y += 10;

        JScrollPane passwordScroll = makeTextField(new JTextArea(), w, 195);
        passwordScroll.setBounds(x, y, w, 195);
        add(passwordScroll);
        y += 205;

        y = addLabel("To decrypt with A5/1 method", GRAY_HINT, Font.PLAIN, 20, x, y, w);
        y += 30;

        y = addLabel("Insert stego-key", BLACK, Font.PLAIN, 20, x, y, w);
        y += 10;

        JScrollPane stegokeyScroll = makeTextField(new JTextArea(), w, 195);
        stegokeyScroll.setBounds(x, y, w, 195);
        add(stegokeyScroll);
        y += 205 + 30;

        decodeBtn = makeActionButton("Decode");
        decodeBtn.setBounds(x, y, w, 42);
        decodeBtn.addActionListener(e -> { if (onDecode != null) onDecode.run(); });
        add(decodeBtn);
        y += 42;

        return y;
    }

    private int addLabel(String text, Color color, int style, int size, int x, int y, int w) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", style, size));
        label.setForeground(color);
        label.setBounds(x, y, w, 26);
        add(label);
        return y + 26;
    }

    private JLabel makeHintLabel(String text, int x, int y, int w) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", Font.PLAIN, 16));
        label.setForeground(GRAY_HINT);
        label.setBounds(x, y, w, 20);
        return label;
    }

    private JButton makeActionButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.PLAIN, 20));
        btn.setForeground(WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }

    private JButton makeBlueButtonRaw(String text, int bw, int bh) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? BLUE : GRAY_TAB);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Inter", Font.PLAIN, 20));
        btn.setForeground(WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }

    private JScrollPane makeTextField(JTextArea area, int w, int h) {
        area.setFont(new Font("Inter", Font.PLAIN, 16));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int bx, int by, int bw2, int bh2) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLUE);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(bx, by, bw2 - 1, bh2 - 1, 20, 20);
                g2.dispose();
            }
            @Override
            public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
        });
        return scroll;
    }

    private JRadioButton makeRadioButton(String text, ButtonGroup group) {
        JRadioButton radio = new JRadioButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = 18;
                int ry = (getHeight() - r) / 2;
                if (isSelected()) {
                    g2.setColor(BLUE);
                    g2.fillOval(0, ry, r, r);
                } else {
                    g2.setColor(WHITE);
                    g2.fillOval(0, ry, r, r);
                    g2.setColor(RADIO_BORDER);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(0, ry, r - 1, r - 1);
                }
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics(getFont());
                g2.drawString(getText(), r + 8, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        radio.setFont(new Font("Inter", Font.PLAIN, 20));
        radio.setForeground(BLACK);
        radio.setOpaque(false);
        radio.setBorderPainted(false);
        radio.setContentAreaFilled(false);
        radio.setFocusPainted(false);
        group.add(radio);
        return radio;
    }
}