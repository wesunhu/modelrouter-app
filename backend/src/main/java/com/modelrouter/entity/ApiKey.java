package com.modelrouter.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "api_keys")
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "api_keys_gen")
    @TableGenerator(name = "api_keys_gen", table = "id_generator", pkColumnName = "gen_key", valueColumnName = "gen_value", pkColumnValue = "api_keys", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String platform;

    @Column(name = "auth_type")
    private String authType = "api_key";

    @Column(name = "api_endpoint")
    private String apiEndpoint;

    @Column
    private String secret;

    @Column
    private Integer quota;

    @Column(nullable = false)
    private String status = "active";

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_models", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "model_id")
    private List<Long> modelIds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public Integer getQuota() { return quota; }
    public void setQuota(Integer quota) { this.quota = quota; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Long> getModelIds() { return modelIds; }
    public void setModelIds(List<Long> modelIds) { this.modelIds = modelIds != null ? modelIds : new ArrayList<>(); }

    public boolean hasAccessToModel(Long modelId) {
        if (modelIds == null || modelIds.isEmpty()) return true;
        return modelIds.contains(modelId);
    }
}
