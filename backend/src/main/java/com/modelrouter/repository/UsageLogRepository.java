package com.modelrouter.repository;

import com.modelrouter.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
    List<UsageLog> findByRouteId(Long routeId);
    List<UsageLog> findByModelId(Long modelId);
    List<UsageLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
