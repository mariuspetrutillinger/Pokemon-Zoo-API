package com.pokemonzoo.api;

import com.pokemonzoo.api.dto.AppClientDetailsDto;
import com.pokemonzoo.api.dto.LoginRequest;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.ClientFavoritePokemon;
import com.pokemonzoo.api.model.Pokemon;
import com.pokemonzoo.api.repository.AppClientRepository;
import com.pokemonzoo.api.repository.ClientFavoritePokemonRepository;
import com.pokemonzoo.api.repository.PokemonRepository;
import com.pokemonzoo.api.service.AppClientService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppClientServiceTest {

    @Mock
    private AppClientRepository appClientRepository;
    @Mock
    private ClientFavoritePokemonRepository clientFavoritePokemonRepository;
    @Mock
    private PokemonRepository pokemonRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AppClientService appClientService;

    @BeforeEach
    void setUp() {
        appClientService = new AppClientService(
                appClientRepository,
                clientFavoritePokemonRepository,
                pokemonRepository,
                passwordEncoder
        );
    }

    @Test
    void createUser_WithNewUsername_ShouldSaveUser() {
        // Arrange
        AppClient appClient = new AppClient();
        appClient.setUsername("newuser");
        when(appClientRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        // Act
        appClientService.createUser(appClient);

        // Assert
        verify(appClientRepository).save(appClient);
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        AppClient appClient = new AppClient();
        appClient.setUsername("existinguser");
        when(appClientRepository.findByUsername("existinguser"))
                .thenReturn(Optional.of(new AppClient()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> appClientService.createUser(appClient));
        verify(appClientRepository, never()).save(any());
    }

    @Test
    void getUser_WithValidCredentials_ShouldReturnUser() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        AppClient expectedUser = new AppClient();
        expectedUser.setUsername("testuser");
        expectedUser.setPassword("encodedPassword");

        when(appClientRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(expectedUser));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        // Act
        AppClient result = appClientService.getUser(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result);
    }

    @Test
    void getUser_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");
        when(appClientRepository.findByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> appClientService.getUser(loginRequest));
    }

    @Test
    void getUser_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        AppClient user = new AppClient();
        user.setPassword("encodedPassword");

        when(appClientRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> appClientService.getUser(loginRequest));
    }

    @Test
    void getUserDetails_WithValidUsername_ShouldReturnDetails() {
        // Arrange
        String username = "testuser";
        UUID userId = UUID.randomUUID();
        AppClient appClient = new AppClient();
        appClient.setId(userId);
        appClient.setUsername(username);

        Pokemon pokemon1 = new Pokemon();
        pokemon1.setName("Pikachu");
        Pokemon pokemon2 = new Pokemon();
        pokemon2.setName("Charizard");

        ClientFavoritePokemon favPokemon1 = new ClientFavoritePokemon();
        favPokemon1.setPokemon(pokemon1);
        ClientFavoritePokemon favPokemon2 = new ClientFavoritePokemon();
        favPokemon2.setPokemon(pokemon2);

        when(appClientRepository.findByUsername(username))
                .thenReturn(Optional.of(appClient));
        when(clientFavoritePokemonRepository.findByClientId(userId))
                .thenReturn(List.of(favPokemon1, favPokemon2));

        // Act
        AppClientDetailsDto result = appClientService.getUserDetails(username);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getClientId());
        assertEquals(username, result.getUsername());
        assertEquals(List.of("Pikachu", "Charizard"), result.getFavoritePokemonList());
    }

    @Test
    void getUserDetails_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        String username = "nonexistent";
        when(appClientRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> appClientService.getUserDetails(username));
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(appClientRepository.existsById(userId)).thenReturn(true);

        // Act
        appClientService.deleteUser(userId);

        // Assert
        verify(appClientRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WithInvalidId_ShouldThrowException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(appClientRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> appClientService.deleteUser(userId));
        verify(appClientRepository, never()).deleteById(any());
    }
}
