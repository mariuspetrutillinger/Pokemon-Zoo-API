package com.pokemonzoo.api;

import com.pokemonzoo.api.dto.PokemonDetailsDto;
import com.pokemonzoo.api.dto.SearchPokemonDTO;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.ClientFavoritePokemon;
import com.pokemonzoo.api.model.Habitat;
import com.pokemonzoo.api.model.Pokemon;
import com.pokemonzoo.api.repository.ClientFavoritePokemonRepository;
import com.pokemonzoo.api.repository.PokemonRepository;
import com.pokemonzoo.api.service.PokemonService;
import com.pokemonzoo.api.service.enums.SearchType;
import com.pokemonzoo.api.service.factory.PokemonSearchFactory;
import com.pokemonzoo.api.service.strategy.PokemonSearchStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokemonRepository pokemonRepository;
    @Mock
    private ClientFavoritePokemonRepository clientFavoritePokemonRepository;
    @Mock
    private PokemonSearchFactory searchFactory;

    @Captor
    private ArgumentCaptor<Pokemon> pokemonCaptor;

    private PokemonService pokemonService;

    @BeforeEach
    void setUp() {
        pokemonService = new PokemonService(
                pokemonRepository,
                clientFavoritePokemonRepository,
                searchFactory
        );
    }

    @Test
    void getAllPokemons_ShouldReturnPagedAndMappedPokemons() {
        // Arrange
        Pokemon pokemon1 = createTestPokemon("Pikachu", "Electric", 5);
        Pokemon pokemon2 = createTestPokemon("Charizard", "Fire", 10);
        List<Pokemon> pokemonList = List.of(pokemon1, pokemon2);

        Page<Pokemon> page = new PageImpl<>(pokemonList);
        when(pokemonRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(clientFavoritePokemonRepository.findByPokemonId(any())).thenReturn(new ArrayList<>());

        // Act
        List<PokemonDetailsDto> result = pokemonService.getAllPokemons(0);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Pikachu", result.get(0).getName());
        assertEquals("Charizard", result.get(1).getName());
        verify(pokemonRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void mapPokemonToPokemonDetailsDto_WithHabitatAndFavorites_ShouldMapCorrectly() {
        // Arrange
        Pokemon pokemon = createTestPokemon("Pikachu", "Electric", 5);
        Habitat habitat = new Habitat();
        habitat.setName("Forest");
        pokemon.setHabitat(habitat);

        AppClient client = new AppClient();
        client.setUsername("trainer1");
        ClientFavoritePokemon favorite = new ClientFavoritePokemon();
        favorite.setClient(client);
        List<ClientFavoritePokemon> favorites = List.of(favorite);

        Page<Pokemon> page = new PageImpl<>(List.of(pokemon));
        when(pokemonRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(clientFavoritePokemonRepository.findByPokemonId(pokemon.getId())).thenReturn(favorites);

        // Act
        List<PokemonDetailsDto> result = pokemonService.getAllPokemons(0);

        // Assert
        assertFalse(result.isEmpty());
        PokemonDetailsDto dto = result.get(0);
        assertEquals("Forest", dto.getHabitatName());
        assertEquals(1, dto.getFavoritedByClientNames().size());
        assertEquals("trainer1", dto.getFavoritedByClientNames().get(0));
    }

    @Test
    void searchPokemons_WithValidParams_ShouldReturnSearchResults() {
        // Arrange
        SearchPokemonDTO searchDto = new SearchPokemonDTO();
        searchDto.setSearchType(SearchType.NAME);
        searchDto.setSearchTerm("Pikachu");
        searchDto.setPage(0);

        Pokemon pokemon = createTestPokemon("Pikachu", "Electric", 5);
        List<Pokemon> searchResults = List.of(pokemon);

        PokemonSearchStrategy searchStrategy = mock(PokemonSearchStrategy.class);
        when(searchFactory.createStrategy(SearchType.NAME)).thenReturn(searchStrategy);
        when(searchStrategy.search(eq("Pikachu"), eq(0), any())).thenReturn(searchResults);
        when(clientFavoritePokemonRepository.findByPokemonId(any())).thenReturn(new ArrayList<>());

        // Act
        List<PokemonDetailsDto> result = pokemonService.searchPokemons(searchDto);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Pikachu", result.get(0).getName());
    }

    @Test
    void searchPokemons_WithNullSearchDto_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> pokemonService.searchPokemons(null));
    }

    @Test
    void searchPokemons_WithNullSearchType_ShouldThrowException() {
        SearchPokemonDTO searchDto = new SearchPokemonDTO();
        searchDto.setSearchTerm("Pikachu");
        searchDto.setPage(0);

        assertThrows(IllegalArgumentException.class, () -> pokemonService.searchPokemons(searchDto));
    }

    @Test
    void countPokemons_WithValidParams_ShouldReturnCount() {
        // Arrange
        SearchPokemonDTO searchDto = new SearchPokemonDTO();
        searchDto.setSearchType(SearchType.NAME);
        searchDto.setSearchTerm("Pikachu");

        PokemonSearchStrategy searchStrategy = mock(PokemonSearchStrategy.class);
        when(searchFactory.createStrategy(SearchType.NAME)).thenReturn(searchStrategy);
        when(searchStrategy.count(eq("Pikachu"), any())).thenReturn(5);

        // Act
        Integer result = pokemonService.countPokemons(searchDto);

        // Assert
        assertEquals(5, result);
    }

    @Test
    void savePokemon_WithValidData_ShouldSavePokemon() {
        // Arrange
        PokemonDetailsDto dto = new PokemonDetailsDto();
        dto.setName("Pikachu");
        dto.setSpecies("Electric");
        dto.setAge(5);
        dto.setWeight(60);
        dto.setHeight(4);

        // Act
        pokemonService.savePokemon(dto);

        // Assert
        verify(pokemonRepository).save(pokemonCaptor.capture());
        Pokemon savedPokemon = pokemonCaptor.getValue();
        assertEquals("Pikachu", savedPokemon.getName());
        assertEquals("Electric", savedPokemon.getSpecies());
        assertEquals(5, savedPokemon.getAge());
        assertEquals(60, savedPokemon.getWeight());
        assertEquals(4, savedPokemon.getHeight());
    }

    private Pokemon createTestPokemon(String name, String species, int age) {
        Pokemon pokemon = new Pokemon();
        pokemon.setId(UUID.randomUUID());
        pokemon.setName(name);
        pokemon.setSpecies(species);
        pokemon.setAge(age);
        pokemon.setWeight(100);
        pokemon.setHeight(10);
        return pokemon;
    }
}