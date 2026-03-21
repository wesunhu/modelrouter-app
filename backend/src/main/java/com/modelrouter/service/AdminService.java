/**
 * Admin user creation and password verification.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.service;

import com.modelrouter.entity.Admin;
import com.modelrouter.repository.AdminRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 管理员服务：密码使用 BCrypt 加密存储
 */
@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public boolean verifyPassword(String username, String rawPassword) {
        return adminRepository.findByUsername(username)
                .map(admin -> passwordEncoder.matches(rawPassword, admin.getPasswordHash()))
                .orElse(false);
    }

    public Admin createAdmin(String username, String rawPassword) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        return adminRepository.save(admin);
    }

    public boolean hasAnyAdmin() {
        return adminRepository.count() > 0;
    }
}
