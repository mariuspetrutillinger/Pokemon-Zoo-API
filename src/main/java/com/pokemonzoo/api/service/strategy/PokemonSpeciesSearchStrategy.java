package com.pokemonzoo.api.service.strategy;

import com.pokemonzoo.api.model.Pokemon;
import com.pokemonzoo.api.repository.PokemonRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PokemonSpeciesSearchStrategy implements PokemonSearchStrategy {
    @Override
    public List<Pokemon> search(String searchTerm, Integer page, PokemonRepository repository) {
        Pageable pageable = PageRequest.of(page, 10);
        return repository.findBySpecies(searchTerm, pageable)
                .orElseThrow(() -> new IllegalArgumentException("No pokemons found with the given species"))
                .getContent();
    }

    @Override
    public Integer count(String searchTerm, PokemonRepository repository) {
        return repository.countAllBySpeciesEqualsIgnoreCase(searchTerm);
    }
}
