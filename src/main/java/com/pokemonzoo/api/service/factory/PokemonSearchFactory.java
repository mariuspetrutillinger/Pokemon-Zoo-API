package com.pokemonzoo.api.service.factory;

import com.pokemonzoo.api.service.enums.SearchType;
import com.pokemonzoo.api.service.strategy.PokemonSearchStrategy;

public interface PokemonSearchFactory {
    PokemonSearchStrategy createStrategy(SearchType searchType);
}
