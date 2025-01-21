package com.pokemonzoo.api.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HabitatDetailsDto {
    private UUID habitatId;
    private String name;
    private String description;
    private Double foodSupply;
    private List<String> pokemonInHabitatNames;
    private List<String> donatorNames;
}
