package com.modelrouter.service;

import com.modelrouter.entity.Model;
import com.modelrouter.repository.ModelRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 模型服务：带缓存，减少 DB 查询
 */
@Service
public class ModelService {

    private final ModelRepository modelRepository;

    public ModelService(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @Cacheable(value = "modelById", key = "#id", unless = "#result == null")
    public Optional<Model> findById(Long id) {
        return modelRepository.findById(id);
    }

    @Cacheable(value = "modelsByStatus", key = "#status")
    public List<Model> findByStatus(String status) {
        return modelRepository.findByStatus(status);
    }

    @Cacheable(value = "modelsByIds", key = "#ids.toString()", unless = "#result.isEmpty()")
    public List<Model> findAllById(Iterable<Long> ids) {
        return modelRepository.findAllById(ids);
    }

    public List<Model> findAll() {
        return modelRepository.findAll();
    }

    public boolean existsById(Long id) {
        return modelRepository.existsById(id);
    }

    @CacheEvict(cacheNames = {"modelById", "modelsByStatus", "modelsByIds"}, allEntries = true)
    public Model save(Model model) {
        return modelRepository.save(model);
    }

    @CacheEvict(cacheNames = {"modelById", "modelsByStatus", "modelsByIds"}, allEntries = true)
    public void deleteById(Long id) {
        modelRepository.deleteById(id);
    }

    /** Provider 变更时调用，清空所有模型缓存 */
    @CacheEvict(cacheNames = {"modelById", "modelsByStatus", "modelsByIds"}, allEntries = true)
    public void evictAll() {
        // @CacheEvict 通过自调用触发
    }
}
