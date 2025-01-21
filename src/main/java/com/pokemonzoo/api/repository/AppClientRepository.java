package com.pokemonzoo.api.repository;

import com.pokemonzoo.api.model.AppClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppClientRepository extends JpaRepository<AppClient, UUID> {
    Optional<AppClient> findByUsername(String username);
}