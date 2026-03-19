package com.modelrouter.controller;

import com.modelrouter.entity.Provider;
import com.modelrouter.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @GetMapping
    public ResponseEntity<List<Provider>> list() {
        return ResponseEntity.ok(providerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Provider> get(@PathVariable Long id) {
        return providerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Provider> create(@RequestBody Provider provider) {
        return ResponseEntity.ok(providerService.save(provider));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Provider> update(@PathVariable Long id, @RequestBody Provider provider) {
        return providerService.findById(id)
                .map(existing -> {
                    existing.setName(provider.getName());
                    existing.setBaseUrl(provider.getBaseUrl());
                    existing.setApiType(provider.getApiType());
                    existing.setAuthHeader(provider.isAuthHeader());
                    existing.setApiKey(provider.getApiKey());
                    existing.setRegisterUrl(provider.getRegisterUrl());
                    return ResponseEntity.ok(providerService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (providerService.existsById(id)) {
            providerService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
