package com.pokemonzoo.api.repository;

import com.pokemonzoo.api.model.Pokemon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, UUID> {
    Optional<Page<Pokemon>> findByNameIsContainingIgnoreCase(String name, Pageable pageable);

    Optional<Page<Pokemon>> findBySpecies(String species, Pageable pageable);

    Integer countAllByNameIsContainingIgnoreCase(String name);

    Integer countAllBySpeciesEqualsIgnoreCase(String species);
}