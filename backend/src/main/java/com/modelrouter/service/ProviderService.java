/**
 * Business logic for Provider persistence.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.service;

import com.modelrouter.entity.Provider;
import com.modelrouter.repository.ProviderRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 平台服务：带缓存；Provider 变更时需清 Model 缓存（Model 关联 Provider）
 */
@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final ModelService modelService;

    public ProviderService(ProviderRepository providerRepository, ModelService modelService) {
        this.providerRepository = providerRepository;
        this.modelService = modelService;
    }

    @Cacheable(value = "providerById", key = "#id", unless = "#result == null")
    public Optional<Provider> findById(Long id) {
        return providerRepository.findById(id);
    }

    public Optional<Provider> findByName(String name) {
        return providerRepository.findByName(name);
    }

    public List<Provider> findAll() {
        return providerRepository.findAll();
    }

    @CacheEvict(cacheNames = {"providerById"}, allEntries = true)
    public Provider save(Provider provider) {
        Provider saved = providerRepository.save(provider);
        modelService.evictAll();
        return saved;
    }

    @CacheEvict(cacheNames = {"providerById"}, allEntries = true)
    public void deleteById(Long id) {
        providerRepository.deleteById(id);
        modelService.evictAll();
    }

    public boolean existsById(Long id) {
        return providerRepository.existsById(id);
    }
}
