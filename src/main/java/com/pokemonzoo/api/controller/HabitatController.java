package com.pokemonzoo.api.controller;

import com.pokemonzoo.api.dto.HabitatDetailsDto;
import com.pokemonzoo.api.dto.HabitatPokemonRequest;
import com.pokemonzoo.api.service.HabitatService;
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
@RequestMapping("/api/habitats")
@Tag(name = "Habitat", description = "Endpoints for managing habitats")
public class HabitatController {
    private final HabitatService habitatService;

    @Operation(summary = "Get all habitats",
            description = "Get all habitats available")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Habitats retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<List<HabitatDetailsDto>> getAllHabitats() {
        try {
            return ResponseEntity.ok(habitatService.getAllHabitats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Add habitat",
            description = "Add a new habitat (habitat id, pokemon or donors list will be ignored)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Habitat added successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to add habitat"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/add")
    public ResponseEntity<String> addHabitat(@RequestBody HabitatDetailsDto habitat) {
        try {
            habitatService.createHabitat(habitat);
            return ResponseEntity.ok("Habitat added successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get habitat details",
            description = "Get habitat details by habitat name (they are unique)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Habitat details retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to retrieve habitat details"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/details")
    public ResponseEntity<HabitatDetailsDto> getHabitatDetails(@RequestBody String habitatName) {
        try {
            return ResponseEntity.ok(habitatService.getHabitatDetails(habitatName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Add Pokemon to habitat",
            description = "Add Pokemon to habitat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pokemon added to habitat successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to add Pokemon to habitat"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/add-pokemon")
    public ResponseEntity<String> addPokemonToHabitat(@RequestBody HabitatPokemonRequest habitatPokemonRequest) {
        try {
            if (habitatPokemonRequest == null) {
                return ResponseEntity.badRequest().body("Request cannot be null");
            }
            habitatService.addPokemonToHabitat(habitatPokemonRequest);
            return ResponseEntity.ok("Pokemon added to habitat successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
