package com.pokemonzoo.api;

import com.pokemonzoo.api.dto.HabitatDetailsDto;
import com.pokemonzoo.api.dto.HabitatPokemonRequest;
import com.pokemonzoo.api.model.*;
import com.pokemonzoo.api.repository.HabitatRepository;
import com.pokemonzoo.api.repository.PokemonRepository;
import com.pokemonzoo.api.service.HabitatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitatServiceTest {

    @Mock
    private HabitatRepository habitatRepository;
    @Mock
    private PokemonRepository pokemonRepository;

    @Captor
    private ArgumentCaptor<Habitat> habitatCaptor;

    private HabitatService habitatService;

    @BeforeEach
    void setUp() {
        habitatService = new HabitatService(habitatRepository, pokemonRepository);
    }

    @Test
    void createHabitat_WithValidData_ShouldSaveHabitat() {
        // Arrange
        HabitatDetailsDto dto = new HabitatDetailsDto();
        dto.setName("Forest");
        dto.setDescription("A dense forest");
        dto.setFoodSupply(100.0);

        // Act
        habitatService.createHabitat(dto);

        // Assert
        verify(habitatRepository).save(habitatCaptor.capture());
        Habitat savedHabitat = habitatCaptor.getValue();
        assertEquals("Forest", savedHabitat.getName());
        assertEquals("A dense forest", savedHabitat.getDescription());
        assertEquals(100.0, savedHabitat.getFoodSupply());
    }

    @Test
    void getAllHabitats_ShouldReturnMappedHabitats() {
        // Arrange
        Habitat habitat1 = createTestHabitat("Forest", "Forest habitat", 100.0);
        Habitat habitat2 = createTestHabitat("Desert", "Desert habitat", 50.0);

        when(habitatRepository.findAll()).thenReturn(List.of(habitat1, habitat2));

        // Act
        List<HabitatDetailsDto> result = habitatService.getAllHabitats();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Forest", result.get(0).getName());
        assertEquals("Desert", result.get(1).getName());
        assertNotNull(result.get(0).getPokemonInHabitatNames());
        assertNotNull(result.get(0).getDonatorNames());
        assertNotNull(result.get(1).getPokemonInHabitatNames());
        assertNotNull(result.get(1).getDonatorNames());
    }

    @Test
    void getHabitatDetails_WithValidName_ShouldReturnDetails() {
        // Arrange
        Habitat habitat = createTestHabitat("Forest", "Forest habitat", 100.0);

        Pokemon pokemon = new Pokemon();
        pokemon.setName("Pikachu");
        habitat.setPokemonInHabitat(Set.of(pokemon));

        AppClient client = new AppClient();
        client.setUsername("donor1");

        Donation donation = new Donation();
        donation.setClient(client);

        DonationHabitat donationHabitat = new DonationHabitat();
        donationHabitat.setDonation(donation);
        habitat.setHabitatDonations(Set.of(donationHabitat));

        when(habitatRepository.findByName("Forest")).thenReturn(Optional.of(habitat));

        // Act
        HabitatDetailsDto result = habitatService.getHabitatDetails("Forest");

        // Assert
        assertNotNull(result);
        assertEquals("Forest", result.getName());
        assertEquals(100.0, result.getFoodSupply());
        assertNotNull(result.getPokemonInHabitatNames());
        assertEquals(1, result.getPokemonInHabitatNames().size());
        assertEquals("Pikachu", result.getPokemonInHabitatNames().get(0));
        assertNotNull(result.getDonatorNames());
        assertEquals(1, result.getDonatorNames().size());
        assertEquals("donor1", result.getDonatorNames().get(0));
    }

    @Test
    void getHabitatDetails_WithInvalidName_ShouldThrowException() {
        // Arrange
        when(habitatRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> habitatService.getHabitatDetails("NonExistent"));
    }

    @Test
    void addPokemonToHabitat_WithValidData_ShouldAddPokemon() {
        // Arrange
        UUID habitatId = UUID.randomUUID();
        UUID pokemonId1 = UUID.randomUUID();
        UUID pokemonId2 = UUID.randomUUID();
        Set<UUID> pokemonIds = Set.of(pokemonId1, pokemonId2);

        Habitat habitat = createTestHabitat("Forest", "Forest habitat", 100.0);
        habitat.setId(habitatId);

        Pokemon pokemon1 = new Pokemon();
        pokemon1.setId(pokemonId1);
        Pokemon pokemon2 = new Pokemon();
        pokemon2.setId(pokemonId2);

        HabitatPokemonRequest request = new HabitatPokemonRequest();
        request.setHabitatId(habitatId);
        request.setPokemonIds(pokemonIds);

        when(habitatRepository.findById(habitatId)).thenReturn(Optional.of(habitat));
        when(pokemonRepository.findAllById(pokemonIds)).thenReturn(List.of(pokemon1, pokemon2));

        // Act
        habitatService.addPokemonToHabitat(request);

        // Assert
        verify(habitatRepository).save(habitatCaptor.capture());
        Habitat savedHabitat = habitatCaptor.getValue();
        assertEquals(2, savedHabitat.getPokemonInHabitat().size());
    }

    @Test
    void addPokemonToHabitat_WithInvalidHabitat_ShouldThrowException() {
        // Arrange
        UUID habitatId = UUID.randomUUID();
        HabitatPokemonRequest request = new HabitatPokemonRequest();
        request.setHabitatId(habitatId);
        request.setPokemonIds(Set.of(UUID.randomUUID()));

        when(habitatRepository.findById(habitatId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> habitatService.addPokemonToHabitat(request));
        verify(pokemonRepository, never()).findAllById(any());
    }

    @Test
    void mapHabitatToDto_WithNullCollections_ShouldHandleNullSafely() {
        // Arrange
        Habitat habitat = new Habitat();
        habitat.setId(UUID.randomUUID());
        habitat.setName("Forest");
        habitat.setDescription("Forest habitat");
        habitat.setFoodSupply(100.0);
        // Explicitly set collections to null
        habitat.setPokemonInHabitat(null);
        habitat.setHabitatDonations(null);

        when(habitatRepository.findByName("Forest")).thenReturn(Optional.of(habitat));

        // Act
        HabitatDetailsDto result = habitatService.getHabitatDetails("Forest");

        // Assert
        assertNotNull(result);
        assertEquals("Forest", result.getName());
        assertEquals("Forest habitat", result.getDescription());
        assertEquals(100.0, result.getFoodSupply());
        assertNotNull(result.getPokemonInHabitatNames());
        assertTrue(result.getPokemonInHabitatNames().isEmpty());
        assertNotNull(result.getDonatorNames());
        assertTrue(result.getDonatorNames().isEmpty());
    }

    private Habitat createTestHabitat(String name, String description, Double foodSupply) {
        Habitat habitat = new Habitat();
        habitat.setId(UUID.randomUUID());
        habitat.setName(name);
        habitat.setDescription(description);
        habitat.setFoodSupply(foodSupply);
        habitat.setPokemonInHabitat(new HashSet<>());
        habitat.setHabitatDonations(new HashSet<>());
        return habitat;
    }
}