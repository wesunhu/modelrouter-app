package com.modelrouter.controller;

import com.modelrouter.entity.Model;
import com.modelrouter.entity.Provider;
import com.modelrouter.service.ModelService;
import com.modelrouter.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    @Autowired
    private ModelService modelService;
    @Autowired
    private ProviderService providerService;

    @GetMapping
    public ResponseEntity<List<Model>> list() {
        return ResponseEntity.ok(modelService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Model> get(@PathVariable Long id) {
        return modelService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Model model) {
        Provider provider = resolveProvider(model.getProvider());
        if (provider == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", "无效的平台：请选择已存在的平台，或先在「平台管理」中添加平台"
            ));
        }
        model.setProvider(provider);
        return ResponseEntity.ok(modelService.save(model));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Model> update(@PathVariable Long id, @RequestBody Model model) {
        return modelService.findById(id)
                .map(existing -> {
                    existing.setName(model.getName());
                    existing.setModelId(model.getModelId());
                    existing.setModelType(model.getModelType());
                    existing.setContextWindow(model.getContextWindow());
                    existing.setMaxTokens(model.getMaxTokens());
                    existing.setCostInput(model.getCostInput());
                    existing.setCostOutput(model.getCostOutput());
                    existing.setTokenCost(model.getTokenCost());
                    existing.setStatus(model.getStatus());
                    Provider provider = resolveProvider(model.getProvider());
                    if (provider != null) {
                        existing.setProvider(provider);
                    }
                    return ResponseEntity.ok(modelService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Provider resolveProvider(Provider input) {
        if (input == null) return null;
        if (input.getId() != null) {
            var byId = providerService.findById(input.getId()).orElse(null);
            if (byId != null) return byId;
        }
        if (input.getName() != null && !input.getName().isBlank()) {
            return providerService.findByName(input.getName().trim()).orElse(null);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (modelService.existsById(id)) {
            modelService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
