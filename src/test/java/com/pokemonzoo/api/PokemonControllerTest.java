package com.pokemonzoo.api;

import com.pokemonzoo.api.controller.PokemonController;
import com.pokemonzoo.api.dto.PokemonDetailsDto;
import com.pokemonzoo.api.dto.SearchPokemonDTO;
import com.pokemonzoo.api.service.PokemonService;
import com.pokemonzoo.api.service.enums.SearchType;
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
class PokemonControllerTest {

    @Mock
    private PokemonService pokemonService;

    private PokemonController pokemonController;

    @BeforeEach
    void setUp() {
        pokemonController = new PokemonController(pokemonService);
    }

    @Test
    void getAllPokemon_WithValidPage_ShouldReturnPokemons() {
        // Arrange
        List<PokemonDetailsDto> expectedPokemons = Arrays.asList(
                createTestPokemonDto("Pikachu", "ELECTRIC"),
                createTestPokemonDto("Charizard", "FIRE")
        );
        when(pokemonService.getAllPokemons(0)).thenReturn(expectedPokemons);

        // Act
        ResponseEntity<List<PokemonDetailsDto>> response = pokemonController.getAllPokemon(0);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedPokemons, response.getBody());
    }

    @Test
    void getAllPokemon_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(pokemonService.getAllPokemons(any())).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<PokemonDetailsDto>> response = pokemonController.getAllPokemon(0);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void searchPokemon_WithValidRequest_ShouldReturnPokemons() {
        // Arrange
        SearchPokemonDTO searchDto = createTestSearchDto("ELECTRIC", SearchType.SPECIES);
        List<PokemonDetailsDto> expectedPokemons = List.of(
                createTestPokemonDto("Pikachu", "ELECTRIC")
        );
        when(pokemonService.searchPokemons(searchDto)).thenReturn(expectedPokemons);

        // Act
        ResponseEntity<List<PokemonDetailsDto>> response = pokemonController.searchPokemon(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Pikachu", response.getBody().get(0).getName());
    }

    @Test
    void searchPokemon_WithInvalidRequest_ShouldReturnBadRequest() {
        // Arrange
        SearchPokemonDTO searchDto = createTestSearchDto(null, null);
        when(pokemonService.searchPokemons(searchDto))
                .thenThrow(new IllegalArgumentException("Invalid search parameters"));

        // Act
        ResponseEntity<List<PokemonDetailsDto>> response = pokemonController.searchPokemon(searchDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void searchPokemon_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        SearchPokemonDTO searchDto = createTestSearchDto("ELECTRIC", SearchType.SPECIES);
        when(pokemonService.searchPokemons(searchDto))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<PokemonDetailsDto>> response = pokemonController.searchPokemon(searchDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void countPokemon_WithValidRequest_ShouldReturnCount() {
        // Arrange
        SearchPokemonDTO searchDto = createTestSearchDto("ELECTRIC", SearchType.SPECIES);
        when(pokemonService.countPokemons(searchDto)).thenReturn(5);

        // Act
        ResponseEntity<Integer> response = pokemonController.countPokemon(searchDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody());
    }

    @Test
    void countPokemon_WithInvalidRequest_ShouldReturnBadRequest() {
        // Arrange
        SearchPokemonDTO searchDto = createTestSearchDto(null, null);
        when(pokemonService.countPokemons(searchDto))
                .thenThrow(new IllegalArgumentException("Invalid search parameters"));

        // Act
        ResponseEntity<Integer> response = pokemonController.countPokemon(searchDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void countPokemon_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        SearchPokemonDTO searchDto = createTestSearchDto("ELECTRIC", SearchType.SPECIES);
        when(pokemonService.countPokemons(searchDto))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<Integer> response = pokemonController.countPokemon(searchDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addPokemon_WithValidPokemon_ShouldAddSuccessfully() {
        // Arrange
        PokemonDetailsDto pokemon = createTestPokemonDto("Pikachu", "ELECTRIC");
        doNothing().when(pokemonService).savePokemon(any(PokemonDetailsDto.class));

        // Act
        ResponseEntity<String> response = pokemonController.addPokemon(pokemon);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pokemon added successfully", response.getBody());
        verify(pokemonService).savePokemon(pokemon);
    }

    @Test
    void addPokemon_WithInvalidData_ShouldReturnBadRequest() {
        // Arrange
        PokemonDetailsDto pokemon = createTestPokemonDto("Pikachu", "ELECTRIC");
        doThrow(new IllegalArgumentException("Invalid pokemon data"))
                .when(pokemonService).savePokemon(any(PokemonDetailsDto.class));

        // Act
        ResponseEntity<String> response = pokemonController.addPokemon(pokemon);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addPokemon_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        PokemonDetailsDto pokemon = createTestPokemonDto("Pikachu", "ELECTRIC");
        doThrow(new RuntimeException("Database error"))
                .when(pokemonService).savePokemon(any(PokemonDetailsDto.class));

        // Act
        ResponseEntity<String> response = pokemonController.addPokemon(pokemon);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addPokemon_WithNullPokemon_ShouldReturnBadRequest() {
        // Act
        ResponseEntity<String> response = pokemonController.addPokemon(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Pokemon data cannot be null", response.getBody());
    }

    private PokemonDetailsDto createTestPokemonDto(String name, String species) {
        PokemonDetailsDto dto = new PokemonDetailsDto();
        dto.setName(name);
        dto.setSpecies(species);
        dto.setAge(5);
        dto.setWeight(100);
        dto.setHeight(5);
        return dto;
    }

    private SearchPokemonDTO createTestSearchDto(String searchTerm, SearchType searchType) {
        SearchPokemonDTO dto = new SearchPokemonDTO();
        dto.setSearchTerm(searchTerm);
        dto.setSearchType(searchType);
        dto.setPage(0);
        return dto;
    }
}