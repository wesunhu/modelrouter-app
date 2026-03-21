/**
 * Loads and stores launcher settings (rootDir, language) in launcher-config.properties next to cwd.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.launcher;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class LauncherConfig {

    private static final String KEY_ROOT = "rootDir";
    private static final String KEY_LANGUAGE = "language";

    private final Path configFile;

    LauncherConfig(Path configDir) {
        this.configFile = configDir.resolve("launcher-config.properties");
    }

    Path getConfigFile() {
        return configFile;
    }

    String loadRootDir() {
        String fromProps = loadPropsRoot();
        if (fromProps != null && !fromProps.isBlank()) {
            return fromProps.trim();
        }
        Path legacy = configFile.getParent().resolve("launcher-config.json");
        return PathsUtil.readRootDirFromLegacyJson(legacy);
    }

    private String loadPropsRoot() {
        if (!Files.isRegularFile(configFile)) {
            return null;
        }
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            return null;
        }
        return p.getProperty(KEY_ROOT);
    }

    void saveRootDir(String rootDir) throws IOException {
        Properties p = loadAllProps();
        p.setProperty(KEY_ROOT, rootDir);
        storeProps(p);
    }

    String loadLanguageTag() {
        if (!Files.isRegularFile(configFile)) {
            return null;
        }
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            return null;
        }
        String v = p.getProperty(KEY_LANGUAGE);
        return v == null ? null : v.trim();
    }

    void saveLanguageTag(String tag) throws IOException {
        Properties p = loadAllProps();
        p.setProperty(KEY_LANGUAGE, tag);
        storeProps(p);
    }

    private Properties loadAllProps() throws IOException {
        Properties p = new Properties();
        if (Files.isRegularFile(configFile)) {
            try (Reader r = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                p.load(r);
            }
        }
        return p;
    }

    private void storeProps(Properties p) throws IOException {
        try (Writer w = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            p.store(w, "ModelRouter Launcher");
        }
    }
}
