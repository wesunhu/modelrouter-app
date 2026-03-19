package com.modelrouter.controller;

import com.modelrouter.entity.ApiKey;
import com.modelrouter.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @GetMapping
    public ResponseEntity<List<ApiKey>> list() {
        return ResponseEntity.ok(apiKeyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiKey> get(@PathVariable Long id) {
        return apiKeyService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiKey> create(@RequestBody ApiKey apiKey) {
        return ResponseEntity.ok(apiKeyService.save(apiKey));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiKey> update(@PathVariable Long id, @RequestBody ApiKey apiKey) {
        return apiKeyService.findById(id)
                .map(existing -> {
                    existing.setKey(apiKey.getKey());
                    existing.setPlatform(apiKey.getPlatform());
                    existing.setAuthType(apiKey.getAuthType());
                    existing.setApiEndpoint(apiKey.getApiEndpoint());
                    existing.setSecret(apiKey.getSecret());
                    existing.setQuota(apiKey.getQuota());
                    existing.setStatus(apiKey.getStatus());
                    existing.setModelIds(apiKey.getModelIds());
                    return ResponseEntity.ok(apiKeyService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (apiKeyService.existsById(id)) {
            apiKeyService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
