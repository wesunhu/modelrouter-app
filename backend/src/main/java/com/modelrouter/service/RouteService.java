package com.modelrouter.service;

import com.modelrouter.entity.Route;
import com.modelrouter.repository.RouteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 路由服务：带缓存，减少 DB 查询
 */
@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Cacheable(value = "routeById", key = "#id", unless = "#result == null")
    public Optional<Route> findById(Long id) {
        return routeRepository.findById(id);
    }

    @Cacheable(value = "routeByApiKey", key = "#apiKey", unless = "#result == null")
    public Optional<Route> findByApiKey(String apiKey) {
        return routeRepository.findByApiKey(apiKey);
    }

    @Cacheable(value = "routes", key = "'status:' + #status")
    public List<Route> findByStatus(String status) {
        return routeRepository.findByStatus(status);
    }

    /** 管理接口用，不缓存 */
    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    public boolean existsById(Long id) {
        return routeRepository.existsById(id);
    }

    @CacheEvict(cacheNames = {"routeById", "routeByApiKey", "routes"}, allEntries = true)
    public Route save(Route route) {
        return routeRepository.save(route);
    }

    @CacheEvict(cacheNames = {"routeById", "routeByApiKey", "routes"}, allEntries = true)
    public void deleteById(Long id) {
        routeRepository.deleteById(id);
    }
}
