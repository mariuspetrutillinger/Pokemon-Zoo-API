package com.pokemonzoo.api;

import com.pokemonzoo.api.controller.HabitatController;
import com.pokemonzoo.api.dto.HabitatDetailsDto;
import com.pokemonzoo.api.dto.HabitatPokemonRequest;
import com.pokemonzoo.api.service.HabitatService;
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
class HabitatControllerTest {

    @Mock
    private HabitatService habitatService;

    private HabitatController habitatController;

    @BeforeEach
    void setUp() {
        habitatController = new HabitatController(habitatService);
    }

    @Test
    void getAllHabitats_WithValidRequest_ShouldReturnHabitats() {
        // Arrange
        List<HabitatDetailsDto> expectedHabitats = Arrays.asList(
                createTestHabitatDto("Forest", "Dense forest"),
                createTestHabitatDto("Desert", "Arid desert")
        );
        when(habitatService.getAllHabitats()).thenReturn(expectedHabitats);

        // Act
        ResponseEntity<List<HabitatDetailsDto>> response = habitatController.getAllHabitats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedHabitats, response.getBody());
    }

    @Test
    void getAllHabitats_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(habitatService.getAllHabitats()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<HabitatDetailsDto>> response = habitatController.getAllHabitats();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addHabitat_WithValidHabitat_ShouldCreateHabitat() {
        // Arrange
        HabitatDetailsDto habitat = createTestHabitatDto("Forest", "Dense forest");
        doNothing().when(habitatService).createHabitat(any(HabitatDetailsDto.class));

        // Act
        ResponseEntity<String> response = habitatController.addHabitat(habitat);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Habitat added successfully", response.getBody());
        verify(habitatService).createHabitat(habitat);
    }

    @Test
    void addHabitat_WithInvalidData_ShouldReturnBadRequest() {
        // Arrange
        HabitatDetailsDto habitat = createTestHabitatDto("Forest", "Dense forest");
        doThrow(new IllegalArgumentException("Invalid habitat data"))
                .when(habitatService).createHabitat(any(HabitatDetailsDto.class));

        // Act
        ResponseEntity<String> response = habitatController.addHabitat(habitat);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addHabitat_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        HabitatDetailsDto habitat = createTestHabitatDto("Forest", "Dense forest");
        doThrow(new RuntimeException("Database error"))
                .when(habitatService).createHabitat(any(HabitatDetailsDto.class));

        // Act
        ResponseEntity<String> response = habitatController.addHabitat(habitat);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getHabitatDetails_WithValidName_ShouldReturnDetails() {
        // Arrange
        String habitatName = "Forest";
        HabitatDetailsDto expectedHabitat = createTestHabitatDto(habitatName, "Dense forest");
        when(habitatService.getHabitatDetails(habitatName)).thenReturn(expectedHabitat);

        // Act
        ResponseEntity<HabitatDetailsDto> response = habitatController.getHabitatDetails(habitatName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(habitatName, response.getBody().getName());
    }

    @Test
    void getHabitatDetails_WithInvalidName_ShouldReturnBadRequest() {
        // Arrange
        String habitatName = "NonExistent";
        when(habitatService.getHabitatDetails(habitatName))
                .thenThrow(new IllegalArgumentException("Habitat not found"));

        // Act
        ResponseEntity<HabitatDetailsDto> response = habitatController.getHabitatDetails(habitatName);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addPokemonToHabitat_WithValidRequest_ShouldAddPokemon() {
        // Arrange
        HabitatPokemonRequest request = createTestHabitatPokemonRequest();
        doNothing().when(habitatService).addPokemonToHabitat(any(HabitatPokemonRequest.class));

        // Act
        ResponseEntity<String> response = habitatController.addPokemonToHabitat(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Pokemon added to habitat successfully", response.getBody());
        verify(habitatService).addPokemonToHabitat(request);
    }

    @Test
    void addPokemonToHabitat_WithInvalidRequest_ShouldReturnBadRequest() {
        // Arrange
        HabitatPokemonRequest request = createTestHabitatPokemonRequest();
        doThrow(new IllegalArgumentException("Invalid request"))
                .when(habitatService).addPokemonToHabitat(any(HabitatPokemonRequest.class));

        // Act
        ResponseEntity<String> response = habitatController.addPokemonToHabitat(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addPokemonToHabitat_WithNullRequest_ShouldReturnBadRequest() {
        // Act
        ResponseEntity<String> response = habitatController.addPokemonToHabitat(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request cannot be null", response.getBody());
    }

    private HabitatDetailsDto createTestHabitatDto(String name, String description) {
        HabitatDetailsDto dto = new HabitatDetailsDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setFoodSupply(100.0);
        dto.setPokemonInHabitatNames(new ArrayList<>());
        dto.setDonatorNames(new ArrayList<>());
        return dto;
    }

    private HabitatPokemonRequest createTestHabitatPokemonRequest() {
        HabitatPokemonRequest request = new HabitatPokemonRequest();
        request.setHabitatId(UUID.randomUUID());
        request.setPokemonIds(Set.of(UUID.randomUUID(), UUID.randomUUID()));
        return request;
    }
}