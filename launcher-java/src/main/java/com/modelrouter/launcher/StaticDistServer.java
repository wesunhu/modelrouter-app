/**
 * Embedded HTTP server for SPA static files; input: port and dist folder; output: HTTP responses.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.launcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serves built SPA from {@code dist} (no Node / npx). Stopping the server reliably releases the port.
 */
final class StaticDistServer {

    private static final Map<String, String> MIME = new HashMap<>();

    static {
        MIME.put("html", "text/html; charset=utf-8");
        MIME.put("htm", "text/html; charset=utf-8");
        MIME.put("js", "application/javascript; charset=utf-8");
        MIME.put("mjs", "application/javascript; charset=utf-8");
        MIME.put("css", "text/css; charset=utf-8");
        MIME.put("json", "application/json; charset=utf-8");
        MIME.put("svg", "image/svg+xml");
        MIME.put("png", "image/png");
        MIME.put("jpg", "image/jpeg");
        MIME.put("jpeg", "image/jpeg");
        MIME.put("gif", "image/gif");
        MIME.put("webp", "image/webp");
        MIME.put("ico", "image/x-icon");
        MIME.put("woff", "font/woff");
        MIME.put("woff2", "font/woff2");
        MIME.put("ttf", "font/ttf");
        MIME.put("txt", "text/plain; charset=utf-8");
        MIME.put("map", "application/json; charset=utf-8");
    }

    private final Path distRoot;
    private HttpServer server;
    private ExecutorService executor;

    StaticDistServer(Path distRoot) {
        this.distRoot = distRoot.toAbsolutePath().normalize();
    }

    synchronized void start(int port) throws IOException {
        if (server != null) {
            return;
        }
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port);
        server = HttpServer.create(addr, 0);
        server.createContext("/", this::handle);
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "static-http");
            t.setDaemon(true);
            return t;
        });
        server.setExecutor(executor);
        server.start();
    }

    synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    synchronized boolean isRunning() {
        return server != null;
    }

    private void handle(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod()) && !"HEAD".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            ex.close();
            return;
        }
        String raw = ex.getRequestURI().getPath();
        if (raw == null || raw.isEmpty()) {
            raw = "/";
        }
        String path = raw.startsWith("/") ? raw.substring(1) : raw;
        Path resolved = safeResolve(path);
        if (resolved == null) {
            ex.sendResponseHeaders(403, -1);
            ex.close();
            return;
        }

        Path file = Files.isRegularFile(resolved) ? resolved : null;
        if (file == null && !hasFileExtension(path)) {
            Path index = distRoot.resolve("index.html");
            if (Files.isRegularFile(index)) {
                file = index;
            }
        }
        if (file == null || !Files.isRegularFile(file)) {
            ex.sendResponseHeaders(404, -1);
            ex.close();
            return;
        }

        String ext = extension(file.getFileName().toString());
        String ctype = MIME.getOrDefault(ext, "application/octet-stream");
        long len = Files.size(file);
        if ("HEAD".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.getResponseHeaders().set("Content-Type", ctype);
            ex.sendResponseHeaders(200, -1);
            ex.close();
            return;
        }
        ex.getResponseHeaders().set("Content-Type", ctype);
        ex.sendResponseHeaders(200, len);
        try (OutputStream os = ex.getResponseBody(); InputStream in = Files.newInputStream(file)) {
            in.transferTo(os);
        }
    }

    private Path safeResolve(String path) {
        if (path.contains("..")) {
            return null;
        }
        Path candidate = distRoot.resolve(path).normalize();
        if (!candidate.startsWith(distRoot)) {
            return null;
        }
        return candidate;
    }

    private static boolean hasFileExtension(String path) {
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        return name.contains(".") && !name.endsWith(".");
    }

    private static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }
}
