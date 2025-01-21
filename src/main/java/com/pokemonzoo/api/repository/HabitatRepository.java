package com.pokemonzoo.api.repository;

import com.pokemonzoo.api.model.Habitat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitatRepository extends JpaRepository<Habitat, UUID> {
    Optional<Habitat> findByName(String name);

    Optional<Habitat> findByDescriptionContainingIgnoreCase(String description);
}