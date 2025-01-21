package com.pokemonzoo.api.service.factory;

import com.pokemonzoo.api.service.enums.SearchType;
import com.pokemonzoo.api.service.strategy.PokemonNameSearchStrategy;
import com.pokemonzoo.api.service.strategy.PokemonSearchStrategy;
import com.pokemonzoo.api.service.strategy.PokemonSpeciesSearchStrategy;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PokemonSearchFactoryImpl implements PokemonSearchFactory {
    private final PokemonNameSearchStrategy nameSearchStrategy;
    private final PokemonSpeciesSearchStrategy speciesSearchStrategy;

    @Override
    public PokemonSearchStrategy createStrategy(SearchType searchType) {
        return switch (searchType) {
            case NAME -> nameSearchStrategy;
            case SPECIES -> speciesSearchStrategy;
        };
    }
}