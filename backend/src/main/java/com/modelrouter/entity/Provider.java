/**
 * JPA entity: AI provider platform definition.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "providers")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "providers_gen")
    @TableGenerator(name = "providers_gen", table = "id_generator", pkColumnName = "gen_key", valueColumnName = "gen_value", pkColumnValue = "providers", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "base_url", nullable = false)
    private String baseUrl;

    @Column(name = "api_type")
    private String apiType = "openai";

    @Column(name = "auth_header")
    private Boolean authHeader = true;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "register_url")
    private String registerUrl;

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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiType() { return apiType; }
    public void setApiType(String apiType) { this.apiType = apiType; }
    public boolean isAuthHeader() { return authHeader == null || authHeader; }
    public void setAuthHeader(Boolean authHeader) { this.authHeader = authHeader; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getRegisterUrl() { return registerUrl; }
    public void setRegisterUrl(String registerUrl) { this.registerUrl = registerUrl; }
}
