package com.modelrouter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "models")
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "models_gen")
    @TableGenerator(name = "models_gen", table = "id_generator", pkColumnName = "gen_key", valueColumnName = "gen_value", pkColumnValue = "models", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String name;

    @Column(name = "model_id", nullable = false)
    private String modelId;

    @Column(name = "model_type")
    private String modelType = "text";

    @Column(name = "context_window")
    private Integer contextWindow = 4096;

    @Column(name = "max_tokens")
    private Integer maxTokens = 2048;

    @Column(name = "cost_input")
    private Double costInput = 0.0;

    @Column(name = "cost_output")
    private Double costOutput = 0.0;

    /** Token 费用：每 token 成本，用于计算成本（优先于 cost_input/cost_output） */
    @Column(name = "token_cost")
    private Double tokenCost;

    public Double getTokenCost() { return tokenCost; }
    public void setTokenCost(Double tokenCost) { this.tokenCost = tokenCost; }

    @Column(nullable = false)
    private String status = "active";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public Integer getContextWindow() { return contextWindow; }
    public void setContextWindow(Integer contextWindow) { this.contextWindow = contextWindow; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public Double getCostInput() { return costInput; }
    public void setCostInput(Double costInput) { this.costInput = costInput; }
    public Double getCostOutput() { return costOutput; }
    public void setCostOutput(Double costOutput) { this.costOutput = costOutput; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
