/**
 * Resolves project root, backend JAR path, and frontend dist directory; input: filesystem paths.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.launcher;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PathsUtil {

    private static final Pattern ROOT_DIR_JSON = Pattern.compile("\"rootDir\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");

    private PathsUtil() {
    }

    static Path resolveRootDir(Path configDir, String savedRoot) {
        if (savedRoot != null && !savedRoot.isBlank()) {
            Path p = Path.of(savedRoot.trim()).toAbsolutePath().normalize();
            if (isValidRootDir(p)) {
                return p;
            }
        }
        Path cwd = configDir.toAbsolutePath().normalize();
        if (isValidRootDir(cwd)) {
            return cwd;
        }
        Path up = cwd;
        for (int i = 0; i < 6; i++) {
            Path parent = up.getParent();
            if (parent == null || parent.equals(up)) {
                break;
            }
            up = parent;
            if (isValidRootDir(up)) {
                return up;
            }
        }
        return cwd;
    }

    static boolean isValidRootDir(Path dir) {
        if (dir == null || !Files.isDirectory(dir)) {
            return false;
        }
        return findJar(dir) != null || findDist(dir) != null;
    }

    static Path findJar(Path rootDir) {
        Path a = rootDir.resolve("modelrouter.jar");
        if (Files.isRegularFile(a)) {
            return a;
        }
        Path b = rootDir.resolve("modelrouter-backend.jar");
        if (Files.isRegularFile(b)) {
            return b;
        }
        Path target = rootDir.resolve("backend").resolve("target");
        if (!Files.isDirectory(target)) {
            return null;
        }
        List<Path> jars = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(target, "modelrouter-backend-*.jar")) {
            for (Path p : stream) {
                String name = p.getFileName().toString();
                if (!name.contains("SNAPSHOT") && !name.contains("original")) {
                    jars.add(p);
                }
            }
        } catch (IOException e) {
            return null;
        }
        jars.sort(Path::compareTo);
        return jars.isEmpty() ? null : jars.get(0);
    }

    static Path findDist(Path rootDir) {
        Path d1 = rootDir.resolve("frontend").resolve("dist");
        if (Files.isRegularFile(d1.resolve("index.html"))) {
            return d1;
        }
        Path d2 = rootDir.resolve("backend").resolve("target").resolve("classes").resolve("static");
        if (Files.isRegularFile(d2.resolve("index.html"))) {
            return d2;
        }
        return null;
    }

    static String readRootDirFromLegacyJson(Path configFile) {
        if (!Files.isRegularFile(configFile)) {
            return null;
        }
        try {
            String text = Files.readString(configFile);
            Matcher m = ROOT_DIR_JSON.matcher(text);
            if (m.find()) {
                return m.group(1).replace("\\\\", "\\");
            }
        } catch (IOException ignored) {
        }
        return null;
    }
}
