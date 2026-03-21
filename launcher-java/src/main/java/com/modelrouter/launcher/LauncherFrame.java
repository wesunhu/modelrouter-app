/**
 * Main Swing window: program path, language selector, start/stop backend and embedded frontend, logs.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.launcher;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;

final class LauncherFrame extends JFrame {

    private static final int BACKEND_PORT = 20118;
    private static final int FRONTEND_PORT = 20119;

    private static final String[] LANG_TAGS = {"zh", "ja", "en"};
    private static final String[] LANG_LABELS = {"中文", "日本語", "English"};

    private final LauncherConfig config = new LauncherConfig(Path.of(System.getProperty("user.dir")));
    private Path rootDir;
    private LauncherI18n i18n;

    private JLabel titleLabel;
    private JLabel lblProgramDirCaption;
    private JLabel lblLanguage;
    private JLabel lblBackend;
    private JLabel lblFrontend;
    private final JLabel pathLabel = new JLabel("-");
    private final JLabel stBackend = statusDot(false);
    private final JLabel stFrontend = statusDot(false);

    private JButton browseBtn;
    private JButton openUiBtn;
    private JComboBox<String> langCombo;
    private boolean langListenerSuppress;

    private JButton btnStartBackend;
    private JButton btnStopBackend;
    private JButton btnStartFrontend;
    private JButton btnStopFrontend;
    private JButton btnStartAll;
    private JButton btnStopAll;

    private JPanel cardsPanel;
    private JScrollPane logScrollPane;

    private final JTextArea logArea = new JTextArea();
    private final BackendProcess backend = new BackendProcess();
    private StaticDistServer frontendServer;

    LauncherFrame() {
        super("");
        i18n = LauncherI18n.fromTag(config.loadLanguageTag());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 520));
        setLocationByPlatform(true);

        String saved = config.loadRootDir();
        rootDir = PathsUtil.resolveRootDir(Path.of(System.getProperty("user.dir")), saved);

        initLayout();
        applyI18n();
        syncLangCombo();
        refreshPathLabel();
        updateButtons();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownQuietly, "launcher-shutdown"));

        appendLog("system", i18n.get("log.ready"));
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        top.add(titleLabel, c);

        c.gridy = 1;
        lblProgramDirCaption = new JLabel();
        top.add(lblProgramDirCaption, c);
        c.gridy = 2;
        pathLabel.setForeground(new Color(0x555555));
        top.add(pathLabel, c);

        c.gridy = 3;
        c.gridwidth = 1;
        c.weightx = 0;
        browseBtn = new JButton();
        browseBtn.addActionListener(e -> chooseRoot());
        top.add(browseBtn, c);

        c.gridx = 1;
        openUiBtn = new JButton();
        openUiBtn.addActionListener(e -> openUri("http://localhost:" + FRONTEND_PORT + "/"));
        top.add(openUiBtn, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        lblLanguage = new JLabel();
        top.add(lblLanguage, c);
        c.gridx = 1;
        langCombo = new JComboBox<>(LANG_LABELS);
        langCombo.addActionListener(e -> onLanguageSelected());
        top.add(langCombo, c);

        root.add(top, BorderLayout.NORTH);

        cardsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        cardsPanel.add(rowBackend(), g);
        g.gridy = 1;
        cardsPanel.add(rowFrontend(), g);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        JPanel bulk = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnStartAll = new JButton();
        btnStopAll = new JButton();
        btnStartAll.addActionListener(e -> startAll());
        btnStopAll.addActionListener(e -> stopAll());
        bulk.add(btnStartAll);
        bulk.add(btnStopAll);
        cardsPanel.add(bulk, g);

        root.add(cardsPanel, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(new Color(0x0d1117));
        logArea.setForeground(new Color(0xc9d1d9));
        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(200, 200));
        root.add(logScrollPane, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel rowBackend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.add(stBackend);
        lblBackend = new JLabel();
        p.add(lblBackend);
        btnStartBackend = new JButton();
        btnStopBackend = new JButton();
        btnStartBackend.addActionListener(e -> startBackend());
        btnStopBackend.addActionListener(e -> stopBackend());
        p.add(btnStartBackend);
        p.add(btnStopBackend);
        return p;
    }

    private JPanel rowFrontend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.add(stFrontend);
        lblFrontend = new JLabel();
        p.add(lblFrontend);
        btnStartFrontend = new JButton();
        btnStopFrontend = new JButton();
        btnStartFrontend.addActionListener(e -> startFrontend());
        btnStopFrontend.addActionListener(e -> stopFrontend());
        p.add(btnStartFrontend);
        p.add(btnStopFrontend);
        return p;
    }

    private static JLabel statusDot(boolean on) {
        JLabel l = new JLabel(on ? "●" : "○");
        l.setForeground(on ? new Color(0x22c55e) : new Color(0x64748b));
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 14f));
        return l;
    }

    private void applyI18n() {
        setTitle(i18n.get("window.title"));
        titleLabel.setText(i18n.get("title.main"));
        lblProgramDirCaption.setText(i18n.get("label.programDir"));
        browseBtn.setText(i18n.get("btn.browse"));
        openUiBtn.setText(i18n.get("btn.openUi"));
        lblLanguage.setText(i18n.get("label.language"));
        lblBackend.setText(i18n.format("label.backend", BACKEND_PORT));
        lblFrontend.setText(i18n.format("label.frontend", FRONTEND_PORT));
        btnStartBackend.setText(i18n.get("btn.startBackend"));
        btnStopBackend.setText(i18n.get("btn.stopBackend"));
        btnStartFrontend.setText(i18n.get("btn.startFrontend"));
        btnStopFrontend.setText(i18n.get("btn.stopFrontend"));
        btnStartAll.setText(i18n.get("btn.startAll"));
        btnStopAll.setText(i18n.get("btn.stopAll"));
        cardsPanel.setBorder(BorderFactory.createTitledBorder(i18n.get("group.services")));
        logScrollPane.setBorder(BorderFactory.createTitledBorder(i18n.get("group.logs")));
        updateFileChooserLocale();
    }

    private void updateFileChooserLocale() {
        Locale loc = switch (i18n.tag()) {
            case "zh" -> Locale.SIMPLIFIED_CHINESE;
            case "ja" -> Locale.JAPAN;
            default -> Locale.ENGLISH;
        };
        JFileChooser.setDefaultLocale(loc);
    }

    private void syncLangCombo() {
        langListenerSuppress = true;
        langCombo.setSelectedIndex(langIndex(i18n.tag()));
        langListenerSuppress = false;
    }

    private static int langIndex(String tag) {
        return switch (tag) {
            case "ja" -> 1;
            case "en" -> 2;
            default -> 0;
        };
    }

    private void onLanguageSelected() {
        if (langListenerSuppress) {
            return;
        }
        int i = langCombo.getSelectedIndex();
        if (i < 0 || i >= LANG_TAGS.length) {
            return;
        }
        String tag = LANG_TAGS[i];
        if (tag.equals(i18n.tag())) {
            return;
        }
        String previous = i18n.tag();
        try {
            config.saveLanguageTag(tag);
        } catch (IOException ex) {
            appendLog("system", i18n.format("log.saveConfigFailed", ex.getMessage()));
            langListenerSuppress = true;
            langCombo.setSelectedIndex(langIndex(previous));
            langListenerSuppress = false;
            return;
        }
        i18n = LauncherI18n.fromTag(tag);
        applyI18n();
    }

    private void refreshPathLabel() {
        pathLabel.setText(rootDir.toString());
    }

    private void chooseRoot() {
        JFileChooser ch = new JFileChooser(rootDir.toFile());
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ch.setDialogTitle(i18n.get("filechooser.title"));
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path sel = ch.getSelectedFile().toPath().toAbsolutePath().normalize();
            if (!PathsUtil.isValidRootDir(sel)) {
                JOptionPane.showMessageDialog(this,
                        i18n.get("error.invalidDir"),
                        i18n.get("error.invalidDir.title"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            rootDir = sel;
            try {
                config.saveRootDir(rootDir.toString());
            } catch (IOException ex) {
                appendLog("system", i18n.format("log.saveConfigFailed", ex.getMessage()));
            }
            refreshPathLabel();
            appendLog("system", i18n.format("log.rootChanged", rootDir));
        }
    }

    private void startBackend() {
        Path jar = PathsUtil.findJar(rootDir);
        if (jar == null) {
            JOptionPane.showMessageDialog(this, i18n.get("error.noJar"), i18n.get("error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (backend.isRunning()) {
            return;
        }
        appendLog("backend", i18n.get("log.startingBackend"));
        try {
            backend.start(rootDir, jar, BACKEND_PORT, line -> appendLog("backend", line));
        } catch (IOException ex) {
            appendLog("backend", i18n.format("log.backendStartFailed", ex.getMessage()));
            JOptionPane.showMessageDialog(this, ex.getMessage(), i18n.get("error.startBackend.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        updateButtons();
    }

    private void stopBackend() {
        backend.stop(line -> appendLog("backend", line));
        updateButtons();
    }

    private void startFrontend() {
        Path dist = PathsUtil.findDist(rootDir);
        if (dist == null) {
            JOptionPane.showMessageDialog(this, i18n.get("error.noDist"), i18n.get("error.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (frontendServer != null && frontendServer.isRunning()) {
            return;
        }
        if (frontendServer != null) {
            frontendServer.stop();
        }
        frontendServer = new StaticDistServer(dist);
        try {
            frontendServer.start(FRONTEND_PORT);
            appendLog("frontend", i18n.format("log.frontendStarted", FRONTEND_PORT));
        } catch (IOException ex) {
            frontendServer = null;
            appendLog("frontend", i18n.format("log.frontendStartFailed", ex.getMessage()));
            JOptionPane.showMessageDialog(this, i18n.format("msg.portInUse", ex.getMessage()),
                    i18n.get("error.startFrontend.title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        updateButtons();
    }

    private void stopFrontend() {
        if (frontendServer != null) {
            frontendServer.stop();
            frontendServer = null;
        }
        appendLog("frontend", i18n.get("log.frontendStopped"));
        updateButtons();
    }

    private void startAll() {
        startBackend();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(2500);
                return null;
            }

            @Override
            protected void done() {
                startFrontend();
            }
        }.execute();
    }

    private void stopAll() {
        stopFrontend();
        stopBackend();
    }

    private void updateButtons() {
        boolean be = backend.isRunning();
        boolean fe = frontendServer != null && frontendServer.isRunning();
        SwingUtilities.invokeLater(() -> {
            stBackend.setText(be ? "●" : "○");
            stBackend.setForeground(be ? new Color(0x22c55e) : new Color(0x64748b));
            stFrontend.setText(fe ? "●" : "○");
            stFrontend.setForeground(fe ? new Color(0x22c55e) : new Color(0x64748b));

            btnStartBackend.setEnabled(!be);
            btnStopBackend.setEnabled(be);
            btnStartFrontend.setEnabled(!fe);
            btnStopFrontend.setEnabled(fe);
        });
    }

    private void appendLog(String source, String line) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + source + "] " + line + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void openUri(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            appendLog("system", i18n.format("log.browserFailed", ex.getMessage()));
        }
    }

    private void shutdownQuietly() {
        if (frontendServer != null) {
            frontendServer.stop();
            frontendServer = null;
        }
        backend.stop(null);
    }
}
