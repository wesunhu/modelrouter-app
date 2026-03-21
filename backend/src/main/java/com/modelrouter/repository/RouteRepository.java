/**
 * Spring Data JPA repository for Route.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.repository;

import com.modelrouter.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByApiKey(String apiKey);
    List<Route> findByStatus(String status);
}
