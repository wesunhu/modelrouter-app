/**
 * Business logic for ApiKey persistence and key rotation helpers.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.service;

import com.modelrouter.entity.ApiKey;
import com.modelrouter.repository.ApiKeyRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * API Key 服务：带缓存，减少 DB 查询
 */
@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Cacheable(value = "apiKeyById", key = "#id", unless = "#result == null")
    public Optional<ApiKey> findById(Long id) {
        return apiKeyRepository.findById(id);
    }

    @Cacheable(value = "apiKeyByKey", key = "#key", unless = "#result == null")
    public Optional<ApiKey> findByKey(String key) {
        return apiKeyRepository.findByKey(key);
    }

    @Cacheable(value = "apiKeyBySecret", key = "#secret", unless = "#result == null")
    public Optional<ApiKey> findBySecret(String secret) {
        return apiKeyRepository.findBySecret(secret);
    }

    public List<ApiKey> findAll() {
        return apiKeyRepository.findAll();
    }

    @CacheEvict(cacheNames = {"apiKeyById", "apiKeyByKey", "apiKeyBySecret"}, allEntries = true)
    public ApiKey save(ApiKey apiKey) {
        return apiKeyRepository.save(apiKey);
    }

    @CacheEvict(cacheNames = {"apiKeyById", "apiKeyByKey", "apiKeyBySecret"}, allEntries = true)
    public void deleteById(Long id) {
        apiKeyRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return apiKeyRepository.existsById(id);
    }
}
