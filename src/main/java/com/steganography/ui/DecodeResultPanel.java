package com.steganography.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;

public class DecodeResultPanel extends JPanel {

    private static final Color BLUE = new Color(0x1172E4);
    private static final Color BLACK = Color.BLACK;
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_TAB = new Color(0xDDDDDD);
    private static final Color FIELD_BG = new Color(0xF5F5F5);

    private final int W = 860;
    private Runnable resizeCallback;

    public DecodeResultPanel() {
        setLayout(null);
        setBackground(WHITE);
    }

    public void setResizeCallback(Runnable cb) { this.resizeCallback = cb; }

    public void showMessage(String message) {
        removeAll();
        int y = buildMessageView(0, 0, W, message);
        updateSize(y + 40);
    }

    public void showFile(File file) {
        removeAll();
        int y = buildFileView(0, 0, W, file);
        updateSize(y + 40);
    }

    private int buildMessageView(int x, int y, int w, String message) {
        y = addLabel("Hidden Message", BLACK, Font.PLAIN, 20, x, y, w);
        y += 10;

        JTextArea messageArea = new JTextArea(message != null ? message : "");
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Inter", Font.PLAIN, 16));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        messageArea.setBackground(FIELD_BG);
        messageArea.setForeground(BLACK);

        JScrollPane scroll = new JScrollPane(messageArea);
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
        scroll.setBounds(x, y, w, 195);
        add(scroll);
        y += 195;

        return y;
    }

    private int buildFileView(int x, int y, int w, File file) {
        y = addLabel("Hidden File", BLACK, Font.PLAIN, 20, x, y, w);
        y += 10;

        JButton saveBtn = makeBlueButtonRaw("Save", 241, 42);
        saveBtn.setBounds(x, y, 241, 42);
        add(saveBtn);

        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save Hidden File");
            if (file != null) fc.setSelectedFile(new File(file.getName()));
            if (fc.showSaveDialog(DecodeResultPanel.this) == JFileChooser.APPROVE_OPTION) {
                File dest = fc.getSelectedFile();
                try {
                    if (file != null) Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    else dest.createNewFile();
                    JOptionPane.showMessageDialog(DecodeResultPanel.this,
                            "File berhasil disimpan ke:\n" + dest.getAbsolutePath(),
                            "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(DecodeResultPanel.this,
                            "Gagal menyimpan file:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        y += 42;
        return y;
    }

    private void updateSize(int h) {
        setPreferredSize(new Dimension(W, h));
        if (getParent() != null) {
            setBounds(getBounds().x, getBounds().y, W, h);
            getParent().revalidate();
            getParent().repaint();
        }
        if (resizeCallback != null) resizeCallback.run();
        revalidate();
        repaint();
    }

    private int addLabel(String text, Color color, int style, int size, int x, int y, int w) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Inter", style, size));
        label.setForeground(color);
        label.setBounds(x, y, w, 26);
        add(label);
        return y + 26;
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
}