/**
 * CRUD REST for Route entities at /api/routes; generates route API key when missing.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.controller;

import com.modelrouter.entity.Route;
import com.modelrouter.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @GetMapping
    public ResponseEntity<List<Route>> list() {
        return ResponseEntity.ok(routeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> get(@PathVariable Long id) {
        return routeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Route> create(@RequestBody Route route) {
        if (route.getApiKey() == null || route.getApiKey().isEmpty()) {
            route.setApiKey("mr-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        }
        return ResponseEntity.ok(routeService.save(route));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> update(@PathVariable Long id, @RequestBody Route route) {
        return routeService.findById(id)
                .map(existing -> {
                    existing.setName(route.getName());
                    if (route.getApiKey() != null && !route.getApiKey().isBlank()) {
                        existing.setApiKey(route.getApiKey());
                    }
                    existing.setPrimaryModelId(route.getPrimaryModelId());
                    existing.setModelType(route.getModelType());
                    existing.setTimeout(route.getTimeout());
                    existing.setTokenSellingPrice(route.getTokenSellingPrice());
                    existing.setStrategy(route.getStrategy());
                    existing.setStatus(route.getStatus());
                    existing.setBackupModelIds(route.getBackupModelIds());
                    return ResponseEntity.ok(routeService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (routeService.existsById(id)) {
            routeService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
