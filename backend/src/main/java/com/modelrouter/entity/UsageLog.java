/**
 * JPA entity: token usage and cost log line.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_logs")
public class UsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "usage_logs_gen")
    @TableGenerator(name = "usage_logs_gen", table = "id_generator", pkColumnName = "gen_key", valueColumnName = "gen_value", pkColumnValue = "usage_logs", allocationSize = 1)
    private Long id;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "route_name")
    private String routeName;

    @Column(name = "model_id")
    private Long modelId;

    @Column(name = "model_name")
    private String modelName;

    @Column
    private String platform;

    @Column(name = "api_key_id")
    private Long apiKeyId;

    @Column(name = "prompt_tokens")
    private Integer promptTokens = 0;

    @Column(name = "completion_tokens")
    private Integer completionTokens = 0;

    @Column(name = "total_tokens")
    private Integer totalTokens = 0;

    @Column
    private Double cost = 0.0;

    /** 记录时的模型 token 成本单价（用于按路由统计） */
    @Column(name = "cost_per_token")
    private Double costPerToken;

    /** 记录时的销售单价（来自路由，用于按路由统计销售额） */
    @Column(name = "selling_price_per_token")
    private Double sellingPricePerToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public Long getModelId() { return modelId; }
    public void setModelId(Long modelId) { this.modelId = modelId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Long getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(Long apiKeyId) { this.apiKeyId = apiKeyId; }
    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }
    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }
    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public Double getCostPerToken() { return costPerToken; }
    public void setCostPerToken(Double costPerToken) { this.costPerToken = costPerToken; }
    public Double getSellingPricePerToken() { return sellingPricePerToken; }
    public void setSellingPricePerToken(Double sellingPricePerToken) { this.sellingPricePerToken = sellingPricePerToken; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
