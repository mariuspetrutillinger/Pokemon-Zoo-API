package com.pokemonzoo.api.repository;

import com.pokemonzoo.api.model.ClientFavoritePokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientFavoritePokemonRepository extends JpaRepository<ClientFavoritePokemon, UUID> {
    List<ClientFavoritePokemon> findByClientId(UUID clientId);
    List<ClientFavoritePokemon> findByPokemonId(UUID pokemonId);
    void deleteByClientIdAndPokemonId(UUID clientId, UUID pokemonId);
}
