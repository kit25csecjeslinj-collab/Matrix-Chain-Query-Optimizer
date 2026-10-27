package com.matrixoptimizer.ui;

import com.matrixoptimizer.model.JoinTreeNode;
import javax.swing.*;
import java.awt.*;

/**
 * Custom Java 2D visualizer for the hierarchical binary join tree.
 * Renders base database tables as leaf nodes and intermediate joins as internal nodes
 * with intermediate cardinalities and operation costs.
 */
public class JoinTreePanel extends JPanel {

    private JoinTreeNode rootNode;

    public JoinTreePanel() {
        setBackground(new Color(15, 23, 42)); // Slate-900
    }

    public void setTree(JoinTreeNode root) {
        this.rootNode = root;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        if (rootNode == null) {
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.drawString("No join tree to display. Run query optimization first.", 40, 80);
            return;
        }

        int width = getWidth();
        int initialY = 60;
        drawNode(g2, rootNode, width / 2, initialY, width / 4, 1);
    }

    private void drawNode(Graphics2D g2, JoinTreeNode node, int x, int y, int xOffset, int depth) {
        if (node == null) return;

        int nodeW = node.isLeaf() ? 120 : 140;
        int nodeH = 46;
        int cornerRadius = 10;

        // Draw branches to children
        int childY = y + 80;
        g2.setStroke(new BasicStroke(1.8f));
        g2.setColor(new Color(71, 85, 105)); // Slate-600

        if (node.getLeft() != null) {
            int leftX = x - xOffset;
            g2.drawLine(x, y + nodeH / 2, leftX, childY - nodeH / 2);
            drawNode(g2, node.getLeft(), leftX, childY, Math.max(40, xOffset / 2), depth + 1);
        }
        if (node.getRight() != null) {
            int rightX = x + xOffset;
            g2.drawLine(x, y + nodeH / 2, rightX, childY - nodeH / 2);
            drawNode(g2, node.getRight(), rightX, childY, Math.max(40, xOffset / 2), depth + 1);
        }

        // Draw current node card
        int cardX = x - nodeW / 2;
        int cardY = y - nodeH / 2;

        if (node.isLeaf()) {
            // Leaf table node (Blue / Teal styling)
            g2.setColor(new Color(30, 41, 59));
            g2.fillRoundRect(cardX, cardY, nodeW, nodeH, cornerRadius, cornerRadius);
            g2.setColor(new Color(56, 189, 248)); // Sky-400 border
            g2.drawRoundRect(cardX, cardY, nodeW, nodeH, cornerRadius, cornerRadius);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(node.getLabel(), cardX + 10, cardY + 18);

            g2.setColor(new Color(148, 163, 184));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(String.format("Card: %,d rows", node.getIntermediateCardinality()), cardX + 10, cardY + 34);
        } else {
            // Internal JOIN node (Indigo / Amber styling)
            g2.setColor(new Color(49, 46, 129, 220)); // Indigo dark
            g2.fillRoundRect(cardX, cardY, nodeW, nodeH, cornerRadius, cornerRadius);
            g2.setColor(new Color(129, 140, 248));
            g2.drawRoundRect(cardX, cardY, nodeW, nodeH, cornerRadius, cornerRadius);

            g2.setColor(new Color(253, 224, 71)); // Amber
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("⋈ " + node.getLabel(), cardX + 8, cardY + 18);

            g2.setColor(new Color(226, 232, 240));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(String.format("Est Cost: %,d", node.getEstimatedCost()), cardX + 8, cardY + 34);
        }
    }
}
