/**
 * Spring Data JPA repository for Model.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.repository;

import com.modelrouter.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelRepository extends JpaRepository<Model, Long> {
    List<Model> findByStatus(String status);
    List<Model> findByProviderNameAndStatus(String providerName, String status);
}
