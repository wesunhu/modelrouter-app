/**
 * CRUD REST for Model entities at /api/models; input: CreateModelRequest JSON.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.controller;

import com.modelrouter.dto.CreateModelRequest;
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
    public ResponseEntity<?> create(@RequestBody CreateModelRequest req) {
        if (req.getName() == null || req.getName().isBlank()
                || req.getModelId() == null || req.getModelId().isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error", "name and modelId are required"));
        }
        Model model = new Model();
        applyRequest(model, req);
        Provider provider = resolveProvider(req.getProvider());
        if (provider == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", "Invalid provider: select existing or add one in Providers"
            ));
        }
        model.setProvider(provider);
        return ResponseEntity.ok(modelService.save(model));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Model> update(@PathVariable Long id, @RequestBody CreateModelRequest req) {
        return modelService.findById(id)
                .map(existing -> {
                    applyRequest(existing, req);
                    Provider provider = resolveProvider(req.getProvider());
                    if (provider != null) {
                        existing.setProvider(provider);
                    }
                    return ResponseEntity.ok(modelService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void applyRequest(Model target, CreateModelRequest req) {
        if (req.getName() != null) target.setName(req.getName());
        if (req.getModelId() != null) target.setModelId(req.getModelId());
        if (req.getModelType() != null) target.setModelType(req.getModelType());
        if (req.getContextWindow() != null) target.setContextWindow(req.getContextWindow());
        if (req.getMaxTokens() != null) target.setMaxTokens(req.getMaxTokens());
        if (req.getCostInput() != null) target.setCostInput(req.getCostInput());
        if (req.getCostOutput() != null) target.setCostOutput(req.getCostOutput());
        if (req.getTokenCost() != null) target.setTokenCost(req.getTokenCost());
        if (req.getStatus() != null) target.setStatus(req.getStatus());
    }

    private Provider resolveProvider(CreateModelRequest.ProviderRef input) {
        if (input == null) return null;
        if (input.getId() != null && input.getId() > 0) {
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
