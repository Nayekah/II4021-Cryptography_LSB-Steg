package com.steganography.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

public class MainFrame extends JFrame {

    private static final Color BLUE = new Color(0x1172E4);
    private static final Color BLACK = Color.BLACK;
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_TAB = new Color(0xDDDDDD);

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private EncodePanel encodePanel;
    private DecodePanel decodePanel;
    private EncodeResultPanel encodeResultPanel;
    private DecodeResultPanel decodeResultPanel;
    private boolean encodeActive = true;

    private JPanel encodeTab;
    private JPanel decodeTab;
    private JPanel content;

    public MainFrame() {
        setTitle("LSB Encoder & Decoder");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WHITE);

        JScrollPane scrollPane = new JScrollPane(buildContent());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(scrollPane, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);
    }

    private JPanel buildContent() {
        int x = 70;
        int w = 860;

        encodePanel = new EncodePanel();
        decodePanel = new DecodePanel();
        encodeResultPanel = new EncodeResultPanel();
        decodeResultPanel = new DecodeResultPanel();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(WHITE);
        cardPanel.add(encodePanel, "encode");
        cardPanel.add(decodePanel, "decode");
        cardPanel.add(encodeResultPanel, "encodeResult");
        cardPanel.add(decodeResultPanel, "decodeResult");

        content = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                Component visible = getVisibleCard();
                int cardH = visible != null ? visible.getPreferredSize().height : 600;
                int headerH = 40 + 50 + 30 + 30 + 30 + 44 + 15;
                return new Dimension(1000, headerH + cardH + 40);
            }
        };
        content.setBackground(WHITE);

        int y = 40;

        JLabel title = new JLabel("LSB Encoder & Decoder", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 40));
        title.setForeground(BLUE);
        title.setBounds(x, y, w, 50);
        content.add(title);
        y += 50 + 30;

        JLabel subtitle = new JLabel("Embed and Extract Hidden Data from Least Significant Bits for an AVI Video File", SwingConstants.CENTER);
        subtitle.setFont(new Font("Inter", Font.PLAIN, 20));
        subtitle.setForeground(BLACK);
        subtitle.setBounds(x, y, w, 30);
        content.add(subtitle);
        y += 30 + 30;

        JPanel tabBar = buildTabBar(w);
        tabBar.setBounds(x, y, w, 44);
        content.add(tabBar);
        y += 44 + 15;

        cardPanel.setBounds(x, y, w, encodePanel.getPreferredSize().height);
        content.add(cardPanel);

        encodePanel.setResizeCallback(() -> updateCardSize());
        decodePanel.setResizeCallback(() -> updateCardSize());
        encodeResultPanel.setResizeCallback(() -> updateCardSize());
        decodeResultPanel.setResizeCallback(() -> updateCardSize());

        encodePanel.setOnEncode(() -> showEncodeResult(null));
        decodePanel.setOnDecode(() -> {
            if (decodePanel.isResultText()) {
                showDecodeResult("", null);
            } else {
                showDecodeResult(null, new java.io.File("output"));
            }
        });

        return content;
    }

    public void showDecodeResult(String message, File file) {
        if (file != null) decodeResultPanel.showFile(file);
        else decodeResultPanel.showMessage(message != null ? message : "");

        boolean[] encodeActiveArr = (boolean[]) encodeTab.getClientProperty("active");
        boolean[] decodeActiveArr = (boolean[]) decodeTab.getClientProperty("active");
        encodeActiveArr[0] = false;
        decodeActiveArr[0] = true;
        encodeTab.repaint();
        decodeTab.repaint();
        encodeActive = false;

        cardLayout.show(cardPanel, "decodeResult");
        updateCardSize();
    }

    public void showEncodeResult(File stegoFile) {
        encodeResultPanel.setStegoVideoFile(stegoFile);

        boolean[] encodeActiveArr = (boolean[]) encodeTab.getClientProperty("active");
        boolean[] decodeActiveArr = (boolean[]) decodeTab.getClientProperty("active");
        encodeActiveArr[0] = true;
        decodeActiveArr[0] = false;
        encodeTab.repaint();
        decodeTab.repaint();
        encodeActive = true;

        cardLayout.show(cardPanel, "encodeResult");
        updateCardSize();
    }

    private JPanel buildTabBar(int w) {
        JPanel tabBar = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GRAY_TAB);
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        tabBar.setBackground(WHITE);

        encodeTab = makeTab("Encode", true);
        encodeTab.setBounds(0, 0, 111, 44);
        encodeTab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        encodeTab.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!encodeActive) switchTo("encode");
                else switchTo("encode");
            }
        });
        tabBar.add(encodeTab);

        decodeTab = makeTab("Decode", false);
        decodeTab.setBounds(111, 0, 111, 44);
        decodeTab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        decodeTab.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (encodeActive) switchTo("decode");
            }
        });
        tabBar.add(decodeTab);

        return tabBar;
    }

    private JPanel makeTab(String text, boolean active) {
        boolean[] isActive = {active};
        JPanel tab = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive[0]) {
                    g2.setColor(WHITE);
                    RoundRectangle2D rr = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() + 10, 20, 20);
                    g2.fill(rr);
                    g2.setColor(GRAY_TAB);
                    g2.setStroke(new BasicStroke(1));
                    g2.draw(rr);
                    g2.setColor(WHITE);
                    g2.fillRect(0, getHeight() - 2, getWidth(), 4);
                } else {
                    g2.setColor(WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        tab.setOpaque(false);
        tab.setLayout(new BorderLayout());

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.PLAIN, 20));
        label.setForeground(BLACK);
        tab.add(label, BorderLayout.CENTER);

        tab.putClientProperty("active", isActive);
        return tab;
    }

    private void switchTo(String card) {
        encodeActive = card.equals("encode") || card.equals("encodeResult");

        boolean[] encodeActiveArr = (boolean[]) encodeTab.getClientProperty("active");
        boolean[] decodeActiveArr = (boolean[]) decodeTab.getClientProperty("active");
        encodeActiveArr[0] = encodeActive;
        decodeActiveArr[0] = !encodeActive;

        encodeTab.repaint();
        decodeTab.repaint();

        cardLayout.show(cardPanel, card);
        updateCardSize();
    }

    private void updateCardSize() {
        Component visible = getVisibleCard();
        if (visible == null) return;
        int newH = visible.getPreferredSize().height;
        cardPanel.setPreferredSize(new Dimension(860, newH));
        Rectangle b = cardPanel.getBounds();
        cardPanel.setBounds(b.x, b.y, 860, newH);
        content.revalidate();
        content.repaint();
    }

    private Component getVisibleCard() {
        for (Component c : cardPanel.getComponents()) {
            if (c.isVisible()) return c;
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}