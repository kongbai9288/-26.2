package project;

import javax.swing.*;

/**
 * LowYSwampHut GUI 启动器。
 * 作为应用程序入口，初始化并展示主窗口。
 */
public class Launcher {
    public static void main(String[] args) {
        // 确保 SeedChecker 在主线程中预先初始化（避免多线程下 log4j 报错）
        try {
            SeedCheckerInitializer.initialize();
        } catch (Exception e) {
            System.err.println("Warning: SeedChecker pre-init failed: " + e.getMessage());
        }

        // 在 EDT 线程中启动 GUI
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            LowYSwampHutForFixedSeed frame = new LowYSwampHutForFixedSeed();
            frame.setVisible(true);
        });
    }
}
