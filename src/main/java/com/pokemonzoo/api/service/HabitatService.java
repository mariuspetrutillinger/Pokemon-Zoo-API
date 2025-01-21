package com.pokemonzoo.api.service;

import com.pokemonzoo.api.dto.HabitatDetailsDto;
import com.pokemonzoo.api.dto.HabitatPokemonRequest;
import com.pokemonzoo.api.model.*;
import com.pokemonzoo.api.repository.HabitatRepository;
import com.pokemonzoo.api.repository.PokemonRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class HabitatService {
    private final HabitatRepository habitatRepository;
    private final PokemonRepository pokemonRepository;

    private HabitatDetailsDto mapHabitatToHabitatDetailsDto(Habitat habitat) {
        HabitatDetailsDto habitatDetailsDto = new HabitatDetailsDto();
        habitatDetailsDto.setHabitatId(habitat.getId());
        habitatDetailsDto.setName(habitat.getName());
        habitatDetailsDto.setDescription(habitat.getDescription());
        habitatDetailsDto.setFoodSupply(habitat.getFoodSupply());

        // Initialize with empty list if null
        habitatDetailsDto.setPokemonInHabitatNames(
                habitat.getPokemonInHabitat() != null
                        ? habitat.getPokemonInHabitat().stream()
                        .map(Pokemon::getName)
                        .toList()
                        : new ArrayList<>()
        );

        // Initialize with empty list if null
        habitatDetailsDto.setDonatorNames(
                habitat.getHabitatDonations() != null
                        ? habitat.getHabitatDonations().stream()
                        .map(DonationHabitat::getDonation)
                        .map(Donation::getClient)
                        .map(AppClient::getUsername)
                        .toList()
                        : new ArrayList<>()
        );

        return habitatDetailsDto;
    }

    private List<HabitatDetailsDto> mapListHabitatToHabitatDetailsDto(List<Habitat> habitats) {
        List<HabitatDetailsDto> habitatDetailsDtos = new ArrayList<>();
        for (Habitat habitat : habitats) {
            habitatDetailsDtos.add(mapHabitatToHabitatDetailsDto(habitat));
        }
        return habitatDetailsDtos;
    }

    public void createHabitat(HabitatDetailsDto habitatDetailsDto) {
        Habitat habitat = new Habitat();
        habitat.setName(habitatDetailsDto.getName());
        habitat.setDescription(habitatDetailsDto.getDescription());
        habitat.setFoodSupply(habitatDetailsDto.getFoodSupply());

        habitatRepository.save(habitat);
    }

    public List<HabitatDetailsDto> getAllHabitats() {
        List<Habitat> habitats = habitatRepository.findAll();
        return mapListHabitatToHabitatDetailsDto(habitats);
    }

    public HabitatDetailsDto getHabitatDetails(String habitatName) {
        Habitat habitat = habitatRepository.findByName(habitatName)
                .orElseThrow(() -> new IllegalArgumentException("Habitat not found"));

        return mapHabitatToHabitatDetailsDto(habitat);
    }

    public void addPokemonToHabitat(HabitatPokemonRequest habitatPokemonRequest) {
        UUID habitatId = habitatPokemonRequest.getHabitatId();
        Set<UUID> pokemonIds = habitatPokemonRequest.getPokemonIds();

        Habitat habitat = habitatRepository.findById(habitatId)
                .orElseThrow(() -> new IllegalArgumentException("Habitat not found"));

        List<Pokemon> pokemonList = pokemonRepository.findAllById(pokemonIds);

        for (Pokemon pokemon : pokemonList) {
            pokemon.setHabitat(habitat);
            habitat.getPokemonInHabitat().add(pokemon);
        }

        habitatRepository.save(habitat);
    }
}
