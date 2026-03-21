/**
 * Desktop launcher entry point; invokes the Swing UI on the event dispatch thread.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.launcher;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class LauncherMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            LauncherFrame frame = new LauncherFrame();
            frame.setVisible(true);
        });
    }
}
