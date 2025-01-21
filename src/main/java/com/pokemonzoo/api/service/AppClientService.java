package com.pokemonzoo.api.service;

import com.pokemonzoo.api.dto.AppClientDetailsDto;
import com.pokemonzoo.api.dto.LoginRequest;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.ClientFavoritePokemon;
import com.pokemonzoo.api.model.Pokemon;
import com.pokemonzoo.api.repository.AppClientRepository;
import com.pokemonzoo.api.repository.ClientFavoritePokemonRepository;
import com.pokemonzoo.api.repository.PokemonRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class AppClientService {

    private final AppClientRepository appClientRepository;
    private final ClientFavoritePokemonRepository clientFavoritePokemonRepository;
    private final PokemonRepository pokemonRepository;
    private final PasswordEncoder passwordEncoder;

    public void createUser(AppClient appClient) {
        if (appClientRepository.findByUsername(appClient.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        appClientRepository.save(appClient);
    }

    @Transactional(readOnly = true)
    public AppClient getUser(LoginRequest loginCredentials) {
        String username = loginCredentials.getUsername();
        AppClient foundUser = appClientRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginCredentials.getPassword(), foundUser.getPassword())) {
            throw new EntityNotFoundException("Invalid credentials");
        }

        return foundUser;
    }

    public AppClientDetailsDto getUserDetails(String username) {
        AppClient appClient = appClientRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        List<Pokemon> favoritePokemon = clientFavoritePokemonRepository.findByClientId(appClient.getId())
                .stream()
                .map(ClientFavoritePokemon::getPokemon)
                .toList();

        List<String> favoritePokemonNameList = favoritePokemon.stream()
                .map(Pokemon::getName)
                .toList();

        return new AppClientDetailsDto(appClient.getId(), appClient.getUsername(), favoritePokemonNameList);
    }

    public void deleteUser(UUID userId) {
        if (!appClientRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        appClientRepository.deleteById(userId);
    }
}