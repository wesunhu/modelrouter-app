package com.modelrouter.repository;

import com.modelrouter.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelRepository extends JpaRepository<Model, Long> {
    List<Model> findByStatus(String status);
    List<Model> findByProviderNameAndStatus(String providerName, String status);
}
