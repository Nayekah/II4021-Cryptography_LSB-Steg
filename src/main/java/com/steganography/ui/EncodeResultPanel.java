package com.steganography.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;

public class EncodeResultPanel extends JPanel {

    private static final Color BLUE = new Color(0x1172E4);
    private static final Color BLACK = Color.BLACK;
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_HINT = new Color(0x676767);
    private static final Color GRAY_TAB = new Color(0xDDDDDD);

    private final int W = 860;
    private Runnable resizeCallback;
    private File stegoVideoFile;

    public EncodeResultPanel() {
        setLayout(null);
        setBackground(WHITE);
        buildAll();
    }

    public void setResizeCallback(Runnable cb) { this.resizeCallback = cb; }

    public void setStegoVideoFile(File file) {
        this.stegoVideoFile = file;
    }

    private void buildAll() {
        int x = 0;
        int y = 0;

        y = addLabel("Stego-Video", BLACK, Font.PLAIN, 20, x, y, W);
        y += 10;

        JButton saveBtn = makeBlueButtonRaw("Save", 241, 42);
        saveBtn.setBounds(x, y, 241, 42);
        add(saveBtn);

        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save Stego-Video");
            fc.setSelectedFile(new File(
                    stegoVideoFile != null ? stegoVideoFile.getName() : "stego_output.avi"
            ));
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "AVI Video Files (*.avi)", "avi"
            ));
            fc.setAcceptAllFileFilterUsed(false);

            if (fc.showSaveDialog(EncodeResultPanel.this) == JFileChooser.APPROVE_OPTION) {
                File dest = fc.getSelectedFile();
                if (!dest.getName().toLowerCase().endsWith(".avi"))
                    dest = new File(dest.getAbsolutePath() + ".avi");
                try {
                    if (stegoVideoFile != null) {
                        Files.copy(stegoVideoFile.toPath(), dest.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        dest.createNewFile();
                    }
                    JOptionPane.showMessageDialog(EncodeResultPanel.this,
                            "File berhasil disimpan ke:\n" + dest.getAbsolutePath(),
                            "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(EncodeResultPanel.this,
                            "Gagal menyimpan file:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        y += 42 + 40;
        setPreferredSize(new Dimension(W, y));
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
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }
}