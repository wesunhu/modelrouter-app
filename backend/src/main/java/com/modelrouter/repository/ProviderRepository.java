/**
 * Spring Data JPA repository for Provider.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.repository;

import com.modelrouter.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByName(String name);
}
