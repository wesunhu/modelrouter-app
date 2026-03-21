/**
 * DTO for creating/updating a model from API requests.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 创建/更新模型请求体，避免直接反序列化 JPA {@link com.modelrouter.entity.Model} 导致 Hibernate 状态异常。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateModelRequest {

    private String name;
    private String modelId;
    private String modelType = "text";
    private Integer contextWindow = 4096;
    private Integer maxTokens = 2048;
    private Double costInput = 0.0;
    private Double costOutput = 0.0;
    private Double tokenCost;
    private String status = "active";
    private ProviderRef provider;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProviderRef {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

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
    public Double getTokenCost() { return tokenCost; }
    public void setTokenCost(Double tokenCost) { this.tokenCost = tokenCost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public ProviderRef getProvider() { return provider; }
    public void setProvider(ProviderRef provider) { this.provider = provider; }
}
