package com.pokemonzoo.api.controller;

import com.pokemonzoo.api.dto.PokemonDetailsDto;
import com.pokemonzoo.api.dto.SearchPokemonDTO;
import com.pokemonzoo.api.service.PokemonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/pokemon")
@Tag(name = "Pokemon", description = "Endpoints for managing Pokemon")
public class PokemonController {
    private final PokemonService pokemonService;

    @Operation(summary = "Get all Pokemon",
            description = "Get all Pokemon available")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pokemons retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<List<PokemonDetailsDto>> getAllPokemon(@RequestParam Integer page) {
        return ResponseEntity.ok(pokemonService.getAllPokemons(page));
    }

    @Operation(summary = "Search Pokemon",
            description = "Search Pokemon by name or type, the parameter should be all uppercase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pokemons retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to retrieve pokemons"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/search")
    public ResponseEntity<List<PokemonDetailsDto>> searchPokemon(
            @RequestBody SearchPokemonDTO searchPokemonDTO) {
        return ResponseEntity.ok(pokemonService.searchPokemons(searchPokemonDTO));
    }

    @Operation(summary = "Count Pokemon",
            description = "Count Pokemon by name or type (do not use page)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pokemons counted successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to count pokemons"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/count")
    public ResponseEntity<Integer> countPokemon(
            @RequestBody SearchPokemonDTO searchPokemonDTO) {
        return ResponseEntity.ok(pokemonService.countPokemons(searchPokemonDTO));
    }

    @Operation(summary = "Add Pokemon",
            description = "Add a new Pokemon (do not specify id, habitatName or favoritedbyClient)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pokemon added successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to add Pokemon"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/add")
    public ResponseEntity<String> addPokemon(@RequestBody PokemonDetailsDto pokemon) {
        if (pokemon == null) {
            return ResponseEntity.badRequest().body("Pokemon data cannot be null");
        }
        pokemonService.savePokemon(pokemon);
        return ResponseEntity.ok("Pokemon added successfully");
    }

}
