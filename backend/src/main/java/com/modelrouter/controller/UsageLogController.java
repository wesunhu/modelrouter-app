package com.modelrouter.controller;

import com.modelrouter.entity.UsageLog;
import com.modelrouter.repository.UsageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usage-logs")
public class UsageLogController {

    @Autowired
    private UsageLogRepository usageLogRepository;

    @GetMapping
    public ResponseEntity<List<UsageLog>> list() {
        return ResponseEntity.ok(usageLogRepository.findAll());
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<UsageLog>> byRoute(@PathVariable Long routeId) {
        return ResponseEntity.ok(usageLogRepository.findByRouteId(routeId));
    }

    @GetMapping("/model/{modelId}")
    public ResponseEntity<List<UsageLog>> byModel(@PathVariable Long modelId) {
        return ResponseEntity.ok(usageLogRepository.findByModelId(modelId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<UsageLog>> byDateRange(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        return ResponseEntity.ok(usageLogRepository.findByCreatedAtBetween(startDate, endDate));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> statistics() {
        List<UsageLog> logs = usageLogRepository.findAll();
        return ResponseEntity.ok(calculateStatistics(logs));
    }

    /** 按路由统计：成本、销售额、利润，含总计 */
    @GetMapping("/statistics/by-route")
    public ResponseEntity<Map<String, Object>> statisticsByRoute() {
        List<UsageLog> logs = usageLogRepository.findAll();
        return ResponseEntity.ok(calculateRouteStatistics(logs));
    }

    private Map<String, Object> calculateRouteStatistics(List<UsageLog> logs) {
        var grouped = logs.stream()
                .filter(l -> l.getRouteId() != null && l.getRouteName() != null)
                .collect(Collectors.groupingBy(l -> l.getRouteId() + ":" + l.getRouteName()));

        List<Map<String, Object>> routeStats = new ArrayList<>();
        double totalCost = 0, totalSales = 0, totalProfit = 0;

        for (var entry : grouped.entrySet()) {
            var list = entry.getValue();
            String routeName = list.get(0).getRouteName();
            Long routeId = list.get(0).getRouteId();

            int totalTokens = list.stream().mapToInt(l -> l.getTotalTokens() != null ? l.getTotalTokens() : 0).sum();
            double cost = list.stream().mapToDouble(l -> l.getCost() != null ? l.getCost() : 0).sum();
            double sales = list.stream().mapToDouble(l -> {
                if (l.getSellingPricePerToken() == null || l.getTotalTokens() == null) return 0;
                return l.getTotalTokens() * l.getSellingPricePerToken();
            }).sum();
            double profit = sales - cost;

            totalCost += cost;
            totalSales += sales;
            totalProfit += profit;

            routeStats.add(Map.of(
                    "routeId", routeId,
                    "routeName", routeName,
                    "totalTokens", totalTokens,
                    "cost", cost,
                    "sales", sales,
                    "profit", profit,
                    "requestCount", list.size()
            ));
        }

        var apiKeyLogs = logs.stream().filter(l -> l.getRouteId() == null).toList();
        if (!apiKeyLogs.isEmpty()) {
            double apiCost = apiKeyLogs.stream().mapToDouble(l -> l.getCost() != null ? l.getCost() : 0).sum();
            int apiTokens = apiKeyLogs.stream().mapToInt(l -> l.getTotalTokens() != null ? l.getTotalTokens() : 0).sum();
            totalCost += apiCost;
            routeStats.add(Map.of(
                    "routeId", (Object) null,
                    "routeName", "API Key 直连",
                    "totalTokens", apiTokens,
                    "cost", apiCost,
                    "sales", 0.0,
                    "profit", -apiCost,
                    "requestCount", apiKeyLogs.size()
            ));
        }

        return Map.of(
                "routeStats", routeStats,
                "totalCost", totalCost,
                "totalSales", totalSales,
                "totalProfit", totalProfit
        );
    }

    private Map<String, Object> calculateStatistics(List<UsageLog> logs) {
        int totalRequests = logs.size();
        int totalPromptTokens = logs.stream().mapToInt(l -> l.getPromptTokens() != null ? l.getPromptTokens() : 0).sum();
        int totalCompletionTokens = logs.stream().mapToInt(l -> l.getCompletionTokens() != null ? l.getCompletionTokens() : 0).sum();
        double totalCost = logs.stream().mapToDouble(l -> l.getCost() != null ? l.getCost() : 0).sum();

        Map<String, Map<String, Object>> modelStats = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getModelName() != null ? l.getModelName() : "unknown",
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            int req = list.size();
                            int pt = list.stream().mapToInt(l -> l.getPromptTokens() != null ? l.getPromptTokens() : 0).sum();
                            int ct = list.stream().mapToInt(l -> l.getCompletionTokens() != null ? l.getCompletionTokens() : 0).sum();
                            double c = list.stream().mapToDouble(l -> l.getCost() != null ? l.getCost() : 0).sum();
                            return Map.<String, Object>of(
                                    "requests", req,
                                    "prompt_tokens", pt,
                                    "completion_tokens", ct,
                                    "total_tokens", pt + ct,
                                    "cost", c
                            );
                        })));

        return Map.of(
                "total_requests", totalRequests,
                "total_prompt_tokens", totalPromptTokens,
                "total_completion_tokens", totalCompletionTokens,
                "total_tokens", totalPromptTokens + totalCompletionTokens,
                "total_cost", totalCost,
                "model_stats", modelStats
        );
    }
}
