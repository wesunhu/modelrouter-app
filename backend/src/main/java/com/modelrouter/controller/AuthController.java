/**
 * Admin session: init, login, logout, needs-init, me; input: JSON credentials; output: session state JSON.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.controller;

import com.modelrouter.entity.Admin;
import com.modelrouter.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminService adminService;
    private static final String SESSION_ATTR_USER = "admin_username";

    public AuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 初始化首个管理员（仅当系统中尚无任何管理员时可用）
     */
    @PostMapping("/init")
    public ResponseEntity<?> init(@RequestBody Map<String, String> body) {
        try {
            if (adminService.hasAnyAdmin()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Admin already exists"));
            }
            String username = body.get("username");
            String password = body.get("password");
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and password required"));
            }
            if (username.length() < 2 || username.length() > 64) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username must be 2-64 characters"));
            }
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
            }
            Admin admin = adminService.createAdmin(username.trim(), password);
            return ResponseEntity.ok(Map.of(
                    "username", admin.getUsername(),
                    "message", "Admin created. Please log in."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("error", "Database connection failed"));
        }
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            if (!adminService.hasAnyAdmin()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please initialize admin first"));
            }
            String username = body.get("username");
            String password = body.get("password");
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and password required"));
            }
            if (!adminService.verifyPassword(username.trim(), password)) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_ATTR_USER, username.trim());
            return ResponseEntity.ok(Map.of("username", username.trim()));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("error", "Database connection failed"));
        }
    }

    /**
     * 检查是否需要初始化首个管理员
     */
    @GetMapping("/needs-init")
    public ResponseEntity<?> needsInit() {
        try {
            boolean needsInit = !adminService.hasAnyAdmin();
            return ResponseEntity.ok(Map.of("needsInit", needsInit));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("error", "Database connection failed", "needsInit", true));
        }
    }

    /**
     * 获取当前登录状态
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        String username = (String) session.getAttribute(SESSION_ATTR_USER);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
        return ResponseEntity.ok(Map.of("username", username));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    public static String getSessionAttrUser() {
        return SESSION_ATTR_USER;
    }
}
