package com.pokemonzoo.api.controller;

import com.pokemonzoo.api.dto.AppClientDetailsDto;
import com.pokemonzoo.api.dto.AppClientPokemonRequest;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.ClientFavoritePokemon;
import com.pokemonzoo.api.service.AppClientService;
import com.pokemonzoo.api.service.ClientFavoritePokemonService;
import com.pokemonzoo.api.util.HttpUtil;
import com.pokemonzoo.api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "AppClient", description = "Endpoints for managing user accounts")
public class AppClientController {
    private final AppClientService appClientService;
    private final ClientFavoritePokemonService clientFavoritePokemonService;
    private final HttpUtil httpUtil;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Get user details",
            description = "Get user details by using the authentication token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to retrieve user details"),
            @ApiResponse(responseCode = "403", description = "Unauthorized")
    })
    @PostMapping("/get_user")
    public ResponseEntity<AppClientDetailsDto> getUser(@RequestBody String authHeader) {
        String token = httpUtil.getAuthorizationHeader(authHeader);
        if (token == null) {
            return ResponseEntity.status(403).build();
        }
        String username = jwtUtil.getUsernameFromToken(token);

        AppClientDetailsDto user = appClientService.getUserDetails(username);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Delete user",
            description = "Delete user by using the client ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to delete user")
    })
    @DeleteMapping()
    public ResponseEntity<String> deleteUser(
            @RequestBody UUID clientId) {
        appClientService.deleteUser(clientId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @Operation(summary = "Add favorite Pokemons",
            description = "Add favorite Pokemons to user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite Pokemons added successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to add favorite Pokemons")
    })
    @PostMapping("/favorites")
    public ResponseEntity<String> addFavoritePokemon(
            @RequestBody AppClientPokemonRequest appClientPokemonRequest) {

        UUID clientId = appClientPokemonRequest.getClientId();
        Set<UUID> pokemonIds = appClientPokemonRequest.getPokemonIds();

        clientFavoritePokemonService.addFavorites(clientId, pokemonIds);
        return ResponseEntity.ok("Favorite Pokemons added successfully");
    }

    @Operation(summary = "Remove favorite Pokemons",
            description = "Remove favorite Pokemons from user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite Pokemons removed successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to remove favorite Pokemons")
    })
    @DeleteMapping("/favorites")
    public ResponseEntity<String> removeFavoritePokemon(
            @RequestBody AppClientPokemonRequest appClientPokemonRequest) {

        UUID clientId = appClientPokemonRequest.getClientId();
        Set<UUID> pokemonIds = appClientPokemonRequest.getPokemonIds();

        try {
            pokemonIds.forEach(pokemonId -> clientFavoritePokemonService.removeFavorite(clientId, pokemonId));
            return ResponseEntity.ok("Favorite Pokemons removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}