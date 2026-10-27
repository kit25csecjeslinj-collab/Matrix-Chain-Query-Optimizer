package com.matrixoptimizer;

import com.matrixoptimizer.ui.MainFrame;
import javax.swing.*;

/**
 * Main application launcher for the Matrix Chain Query Optimizer.
 * Sets modern Look-and-Feel (FlatLaf Dark if available, system L&F fallback)
 * and starts the Swing Event Dispatch Thread (EDT).
 */
public class App {

    public static void main(String[] args) {
        // Attempt to load FlatDarkLaf for modern IDE aesthetic
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Default fallback
            }
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
