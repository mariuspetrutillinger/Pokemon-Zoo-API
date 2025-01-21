package com.pokemonzoo.api;

import com.pokemonzoo.api.controller.AppClientController;
import com.pokemonzoo.api.dto.AppClientDetailsDto;
import com.pokemonzoo.api.dto.AppClientPokemonRequest;
import com.pokemonzoo.api.service.AppClientService;
import com.pokemonzoo.api.service.ClientFavoritePokemonService;
import com.pokemonzoo.api.util.HttpUtil;
import com.pokemonzoo.api.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppClientControllerTest {

    @Mock
    private AppClientService appClientService;
    @Mock
    private ClientFavoritePokemonService clientFavoritePokemonService;
    @Mock
    private HttpUtil httpUtil;
    @Mock
    private JwtUtil jwtUtil;

    private AppClientController appClientController;

    @BeforeEach
    void setUp() {
        appClientController = new AppClientController(
                appClientService,
                clientFavoritePokemonService,
                httpUtil,
                jwtUtil
        );
    }

    @Test
    void getUser_WithValidToken_ShouldReturnUserDetails() {
        // Arrange
        String authHeader = "Bearer valid-token";
        String token = "valid-token";
        String username = "testUser";
        AppClientDetailsDto userDetails = new AppClientDetailsDto(
                UUID.randomUUID(),
                username,
                List.of("Pikachu", "Charizard")
        );

        when(httpUtil.getAuthorizationHeader(authHeader)).thenReturn(token);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(appClientService.getUserDetails(username)).thenReturn(userDetails);

        // Act
        ResponseEntity<AppClientDetailsDto> response = appClientController.getUser(authHeader);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());
    }

    @Test
    void getUser_WithInvalidToken_ShouldReturnForbidden() {
        // Arrange
        String authHeader = "Invalid-Header";
        when(httpUtil.getAuthorizationHeader(authHeader)).thenReturn(null);

        // Act
        ResponseEntity<AppClientDetailsDto> response = appClientController.getUser(authHeader);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getUser_WhenServiceThrowsException_ShouldReturnBadRequest() {
        // Arrange
        String authHeader = "Bearer valid-token";
        String token = "valid-token";
        String username = "testUser";

        when(httpUtil.getAuthorizationHeader(authHeader)).thenReturn(token);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
        when(appClientService.getUserDetails(username)).thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<AppClientDetailsDto> response = appClientController.getUser(authHeader);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        ResponseEntity<String> response = appClientController.deleteUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
        verify(appClientService).deleteUser(userId);
    }

    @Test
    void deleteUser_WhenServiceThrowsException_ShouldReturnBadRequest() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String errorMessage = "User not found";
        doThrow(new RuntimeException(errorMessage)).when(appClientService).deleteUser(userId);

        // Act
        ResponseEntity<String> response = appClientController.deleteUser(userId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void addFavoritePokemon_WithValidRequest_ShouldAddSuccessfully() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Set<UUID> pokemonIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
        AppClientPokemonRequest request = new AppClientPokemonRequest();
        request.setClientId(clientId);
        request.setPokemonIds(pokemonIds);

        // Act
        ResponseEntity<String> response = appClientController.addFavoritePokemon(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Favorite Pokemons added successfully", response.getBody());
        verify(clientFavoritePokemonService).addFavorites(clientId, pokemonIds);
    }

    @Test
    void addFavoritePokemon_WhenServiceThrowsException_ShouldReturnBadRequest() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Set<UUID> pokemonIds = Set.of(UUID.randomUUID());
        AppClientPokemonRequest request = new AppClientPokemonRequest();
        request.setClientId(clientId);
        request.setPokemonIds(pokemonIds);

        doThrow(new RuntimeException("Error")).when(clientFavoritePokemonService)
                .addFavorites(clientId, pokemonIds);

        // Act
        ResponseEntity<String> response = appClientController.addFavoritePokemon(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void removeFavoritePokemon_WithValidRequest_ShouldRemoveSuccessfully() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Set<UUID> pokemonIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
        AppClientPokemonRequest request = new AppClientPokemonRequest();
        request.setClientId(clientId);
        request.setPokemonIds(pokemonIds);

        // Act
        ResponseEntity<String> response = appClientController.removeFavoritePokemon(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Favorite Pokemons removed successfully", response.getBody());
        verify(clientFavoritePokemonService, times(pokemonIds.size()))
                .removeFavorite(eq(clientId), any(UUID.class));
    }

    @Test
    void removeFavoritePokemon_WhenServiceThrowsException_ShouldReturnBadRequest() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        UUID pokemonId = UUID.randomUUID();
        Set<UUID> pokemonIds = Set.of(pokemonId);
        AppClientPokemonRequest request = new AppClientPokemonRequest();
        request.setClientId(clientId);
        request.setPokemonIds(pokemonIds);

        doThrow(new RuntimeException("Error"))
                .when(clientFavoritePokemonService).removeFavorite(clientId, pokemonId);

        // Act
        ResponseEntity<String> response = appClientController.removeFavoritePokemon(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}