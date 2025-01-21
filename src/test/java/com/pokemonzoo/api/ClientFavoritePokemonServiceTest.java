package com.pokemonzoo.api;

import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.ClientFavoritePokemon;
import com.pokemonzoo.api.model.Pokemon;
import com.pokemonzoo.api.repository.AppClientRepository;
import com.pokemonzoo.api.repository.ClientFavoritePokemonRepository;
import com.pokemonzoo.api.repository.PokemonRepository;
import com.pokemonzoo.api.service.ClientFavoritePokemonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientFavoritePokemonServiceTest {

    @Mock
    private ClientFavoritePokemonRepository clientFavoritePokemonRepository;
    @Mock
    private AppClientRepository appClientRepository;
    @Mock
    private PokemonRepository pokemonRepository;

    private ClientFavoritePokemonService clientFavoritePokemonService;

    @BeforeEach
    void setUp() {
        clientFavoritePokemonService = new ClientFavoritePokemonService(
                clientFavoritePokemonRepository,
                appClientRepository,
                pokemonRepository
        );
    }

    @Test
    void addFavorites_WithValidUserAndPokemon_ShouldSaveFavorites() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID pokemonId1 = UUID.randomUUID();
        UUID pokemonId2 = UUID.randomUUID();
        Set<UUID> pokemonIds = Set.of(pokemonId1, pokemonId2);

        AppClient appClient = new AppClient();
        appClient.setId(userId);

        Pokemon pokemon1 = new Pokemon();
        pokemon1.setId(pokemonId1);
        Pokemon pokemon2 = new Pokemon();
        pokemon2.setId(pokemonId2);
        List<Pokemon> pokemonList = List.of(pokemon1, pokemon2);

        when(appClientRepository.findById(userId)).thenReturn(Optional.of(appClient));
        when(pokemonRepository.findAllById(pokemonIds)).thenReturn(pokemonList);

        // Act
        clientFavoritePokemonService.addFavorites(userId, pokemonIds);

        // Assert
        verify(clientFavoritePokemonRepository, times(2)).save(any(ClientFavoritePokemon.class));
    }

    @Test
    void addFavorites_WithInvalidUser_ShouldThrowEntityNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Set<UUID> pokemonIds = Set.of(UUID.randomUUID());
        when(appClientRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> clientFavoritePokemonService.addFavorites(userId, pokemonIds));
        verify(clientFavoritePokemonRepository, never()).save(any());
    }

    @Test
    void getFavoritePokemonByUserId_ShouldReturnPokemonSet() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Pokemon pokemon1 = new Pokemon();
        Pokemon pokemon2 = new Pokemon();

        ClientFavoritePokemon favorite1 = new ClientFavoritePokemon();
        favorite1.setPokemon(pokemon1);
        ClientFavoritePokemon favorite2 = new ClientFavoritePokemon();
        favorite2.setPokemon(pokemon2);

        List<ClientFavoritePokemon> favorites = List.of(favorite1, favorite2);
        when(clientFavoritePokemonRepository.findByClientId(userId)).thenReturn(favorites);

        // Act
        Set<Pokemon> result = clientFavoritePokemonService.getFavoritePokemonByUserId(userId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(pokemon1));
        assertTrue(result.contains(pokemon2));
    }

    @Test
    void getFavoritePokemonByUserId_WithNoFavorites_ShouldReturnEmptySet() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(clientFavoritePokemonRepository.findByClientId(userId)).thenReturn(Collections.emptyList());

        // Act
        Set<Pokemon> result = clientFavoritePokemonService.getFavoritePokemonByUserId(userId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getFavoritedByClientsByPokemonId_ShouldReturnClientSet() {
        // Arrange
        UUID pokemonId = UUID.randomUUID();
        AppClient client1 = new AppClient();
        AppClient client2 = new AppClient();

        ClientFavoritePokemon favorite1 = new ClientFavoritePokemon();
        favorite1.setClient(client1);
        ClientFavoritePokemon favorite2 = new ClientFavoritePokemon();
        favorite2.setClient(client2);

        List<ClientFavoritePokemon> favorites = List.of(favorite1, favorite2);
        when(clientFavoritePokemonRepository.findByPokemonId(pokemonId)).thenReturn(favorites);

        // Act
        Set<AppClient> result = clientFavoritePokemonService.getFavoritedByClientsByPokemonId(pokemonId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(client1));
        assertTrue(result.contains(client2));
    }

    @Test
    void getFavoritedByClientsByPokemonId_WithNoClients_ShouldReturnEmptySet() {
        // Arrange
        UUID pokemonId = UUID.randomUUID();
        when(clientFavoritePokemonRepository.findByPokemonId(pokemonId)).thenReturn(Collections.emptyList());

        // Act
        Set<AppClient> result = clientFavoritePokemonService.getFavoritedByClientsByPokemonId(pokemonId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void removeFavorite_ShouldCallRepositoryDelete() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID pokemonId = UUID.randomUUID();

        // Act
        clientFavoritePokemonService.removeFavorite(userId, pokemonId);

        // Assert
        verify(clientFavoritePokemonRepository).deleteByClientIdAndPokemonId(userId, pokemonId);
    }
}
