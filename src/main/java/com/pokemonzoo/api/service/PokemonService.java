package com.pokemonzoo.api.service;

import com.pokemonzoo.api.dto.PokemonDetailsDto;
import com.pokemonzoo.api.dto.SearchPokemonDTO;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.ClientFavoritePokemon;
import com.pokemonzoo.api.model.Habitat;
import com.pokemonzoo.api.model.Pokemon;
import com.pokemonzoo.api.repository.ClientFavoritePokemonRepository;
import com.pokemonzoo.api.repository.PokemonRepository;
import com.pokemonzoo.api.service.enums.SearchType;
import com.pokemonzoo.api.service.factory.PokemonSearchFactory;
import com.pokemonzoo.api.service.strategy.PokemonSearchStrategy;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class PokemonService {
    private final PokemonRepository pokemonRepository;
    private final ClientFavoritePokemonRepository clientFavoritePokemonRepository;
    private final PokemonSearchFactory searchFactory;

    private List<PokemonDetailsDto> mapPokemonToPokemonDetailsDto(List<Pokemon> pokemons) {
        return pokemons.stream().map(pokemon -> {
            PokemonDetailsDto dto = new PokemonDetailsDto();
            dto.setPokemonId(pokemon.getId());
            dto.setName(pokemon.getName());
            dto.setSpecies(pokemon.getSpecies());
            dto.setAge(pokemon.getAge());
            dto.setWeight(pokemon.getWeight());
            dto.setHeight(pokemon.getHeight());

            if (pokemon.getHabitat() != null) {
                dto.setHabitatName(pokemon.getHabitat().getName());
            }

            List<String> favoritedByUsers = clientFavoritePokemonRepository.findByPokemonId(pokemon.getId())
                    .stream()
                    .map(ClientFavoritePokemon::getClient)
                    .map(AppClient::getUsername)
                    .toList();

            dto.setFavoritedByClientNames(favoritedByUsers);
            return dto;
        }).toList();
    }

    public List<PokemonDetailsDto> getAllPokemons(Integer page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Pokemon> pageResult = pokemonRepository.findAll(pageable);
        List<Pokemon> content = pageResult.getContent();
        return mapPokemonToPokemonDetailsDto(content);
    }

    private void validateNotNullRequiredParams(SearchPokemonDTO searchPokemonDTO) {
        if (searchPokemonDTO == null) {
            throw new IllegalArgumentException("SearchPokemonDTO is required");
        }
        if (searchPokemonDTO.getSearchType() == null) {
            throw new IllegalArgumentException("SearchType is required");
        }
        if (searchPokemonDTO.getSearchTerm() == null) {
            throw new IllegalArgumentException("SearchTerm is required");
        }
    }

    public List<PokemonDetailsDto> searchPokemons(SearchPokemonDTO searchPokemonDTO) {
        validateNotNullRequiredParams(searchPokemonDTO);

        SearchType searchType = searchPokemonDTO.getSearchType();
        String searchTerm = searchPokemonDTO.getSearchTerm();
        Integer page = searchPokemonDTO.getPage();

        if (page == null) {
            throw new IllegalArgumentException("Page number is required");
        }

        PokemonSearchStrategy strategy = searchFactory.createStrategy(searchType);
        List<Pokemon> content = strategy.search(searchTerm, page, pokemonRepository);
        return mapPokemonToPokemonDetailsDto(content);
    }

    public Integer countPokemons(SearchPokemonDTO searchPokemonDTO) {
        validateNotNullRequiredParams(searchPokemonDTO);

        SearchType searchType = searchPokemonDTO.getSearchType();
        String searchTerm = searchPokemonDTO.getSearchTerm();

        PokemonSearchStrategy strategy = searchFactory.createStrategy(searchType);
        return strategy.count(searchTerm, pokemonRepository);
    }

    public void savePokemon(PokemonDetailsDto pokemonDetailsDto) {
        Pokemon pokemon = new Pokemon();
        pokemon.setName(pokemonDetailsDto.getName());
        pokemon.setSpecies(pokemonDetailsDto.getSpecies());
        pokemon.setAge(pokemonDetailsDto.getAge());
        pokemon.setWeight(pokemonDetailsDto.getWeight());
        pokemon.setHeight(pokemonDetailsDto.getHeight());

        pokemonRepository.save(pokemon);
    }
}
