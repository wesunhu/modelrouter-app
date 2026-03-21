/**
 * JPA entity: routing rule with primary/fallback models and client API key.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "routes_gen")
    @TableGenerator(name = "routes_gen", table = "id_generator", pkColumnName = "gen_key", valueColumnName = "gen_value", pkColumnValue = "routes", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "primary_model_id")
    private Long primaryModelId;

    @Column(name = "model_type")
    private String modelType = "text";

    @Column
    private Integer timeout = 60000;

    @Column
    private String strategy = "primary-first";

    /** Tokens 销售单价：每 token 售价，用于计算销售额 */
    @Column(name = "token_selling_price")
    private Double tokenSellingPrice;

    @Column(nullable = false)
    private String status = "active";

    public Double getTokenSellingPrice() { return tokenSellingPrice; }
    public void setTokenSellingPrice(Double tokenSellingPrice) { this.tokenSellingPrice = tokenSellingPrice; }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "route_backup_models", joinColumns = @JoinColumn(name = "route_id"))
    @Column(name = "model_id")
    private List<Long> backupModelIds = new ArrayList<>();

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public Long getPrimaryModelId() { return primaryModelId; }
    public void setPrimaryModelId(Long primaryModelId) { this.primaryModelId = primaryModelId; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public Integer getTimeout() { return timeout; }
    public void setTimeout(Integer timeout) { this.timeout = timeout; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Long> getBackupModelIds() { return backupModelIds; }
    public void setBackupModelIds(List<Long> backupModelIds) { this.backupModelIds = backupModelIds != null ? backupModelIds : new ArrayList<>(); }
}
