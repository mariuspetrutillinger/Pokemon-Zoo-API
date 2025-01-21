package com.pokemonzoo.api.service;

import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.ClientFavoritePokemon;
import com.pokemonzoo.api.model.Pokemon;
import com.pokemonzoo.api.repository.AppClientRepository;
import com.pokemonzoo.api.repository.ClientFavoritePokemonRepository;
import com.pokemonzoo.api.repository.PokemonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClientFavoritePokemonService {
    private final ClientFavoritePokemonRepository clientFavoritePokemonRepository;
    private final AppClientRepository appClientRepository;
    private final PokemonRepository pokemonRepository;

    public ClientFavoritePokemonService(ClientFavoritePokemonRepository clientFavoritePokemonRepository,
                                        AppClientRepository appClientRepository,
                                        PokemonRepository pokemonRepository) {
        this.clientFavoritePokemonRepository = clientFavoritePokemonRepository;
        this.appClientRepository = appClientRepository;
        this.pokemonRepository = pokemonRepository;
    }

    @Transactional
    public void addFavorites(UUID userId, Set<UUID> pokemonIds) {
        AppClient appClient = appClientRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<Pokemon> pokemonList = pokemonRepository.findAllById(pokemonIds);

        pokemonList.forEach(pokemon -> {
            ClientFavoritePokemon favorite = new ClientFavoritePokemon();
            favorite.setClient(appClient);
            favorite.setPokemon(pokemon);
            clientFavoritePokemonRepository.save(favorite);
        });
    }

    public Set<Pokemon> getFavoritePokemonByUserId(UUID userId) {
        return clientFavoritePokemonRepository.findByClientId(userId).stream()
                .map(ClientFavoritePokemon::getPokemon)
                .collect(Collectors.toSet());
    }

    public Set<AppClient> getFavoritedByClientsByPokemonId(UUID pokemonId) {
        return clientFavoritePokemonRepository.findByPokemonId(pokemonId).stream()
                .map(ClientFavoritePokemon::getClient)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID pokemonId) {
        clientFavoritePokemonRepository.deleteByClientIdAndPokemonId(userId, pokemonId);
    }
}
