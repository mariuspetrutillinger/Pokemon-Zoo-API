package com.pokemonzoo.api.service.strategy;

import com.pokemonzoo.api.model.Pokemon;
import com.pokemonzoo.api.repository.PokemonRepository;

import java.util.List;


public interface PokemonSearchStrategy {
    List<Pokemon> search(String searchTerm, Integer page, PokemonRepository repository);
    Integer count(String searchTerm, PokemonRepository repository);
}