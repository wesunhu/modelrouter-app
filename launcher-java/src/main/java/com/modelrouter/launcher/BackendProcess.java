/**
 * Spawns and stops the backend java -jar process; streams stdout to UI; input: jar path, port.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

final class BackendProcess {

    private final AtomicReference<Process> processRef = new AtomicReference<>();
    private ExecutorService logExecutor;

    synchronized boolean isRunning() {
        Process p = processRef.get();
        return p != null && p.isAlive();
    }

    synchronized void start(Path rootDir, Path jar, int port, Consumer<String> onLog) throws IOException {
        if (isRunning()) {
            return;
        }
        Path data = rootDir.resolve("data");
        Files.createDirectories(data);

        String javaExe = ProcessHandle.current().info().command().orElse(null);
        if (javaExe == null || javaExe.isBlank()) {
            javaExe = "java";
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(javaExe);
        cmd.add("-jar");
        cmd.add(jar.toAbsolutePath().normalize().toString());
        cmd.add("--spring.profiles.active=sqlite");
        cmd.add("--server.port=" + port);
        cmd.add("--modelrouter.serve-spa=false");
        cmd.add("--spring.web.resources.add-mappings=false");

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(rootDir.toFile());
        pb.environment().put("SPRING_PROFILES_ACTIVE", "sqlite");
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        processRef.set(proc);

        Charset cs = StandardCharsets.UTF_8;
        logExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "backend-log");
            t.setDaemon(true);
            return t;
        });
        logExecutor.submit(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), cs))) {
                String line;
                while ((line = br.readLine()) != null) {
                    onLog.accept(line);
                }
            } catch (IOException ignored) {
            }
            try {
                onLog.accept("[Process exited with code " + proc.exitValue() + "]");
            } catch (IllegalThreadStateException ignored) {
            }
        });
    }

    synchronized void stop(Consumer<String> onLog) {
        Process p = processRef.getAndSet(null);
        if (p == null) {
            return;
        }
        if (logExecutor != null) {
            logExecutor.shutdown();
            logExecutor = null;
        }
        p.destroy();
        try {
            boolean ended = p.waitFor(8, java.util.concurrent.TimeUnit.SECONDS);
            if (!ended) {
                p.destroyForcibly();
                p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            p.destroyForcibly();
        }
        if (onLog != null) {
            onLog.accept("Backend process stopped.");
        }
    }
}
