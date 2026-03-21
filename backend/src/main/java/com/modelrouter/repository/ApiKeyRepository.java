/**
 * Spring Data JPA repository for ApiKey.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.repository;

import com.modelrouter.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKey(String key);
    Optional<ApiKey> findBySecret(String secret);
    List<ApiKey> findByPlatform(String platform);
    List<ApiKey> findByStatus(String status);
}
