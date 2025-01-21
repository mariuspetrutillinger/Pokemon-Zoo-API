package com.pokemonzoo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PokemonDetailsDto {
    private UUID pokemonId;
    private String name;
    private String species;
    private Integer age;
    private Integer weight;
    private Integer height;
    private String habitatName;
    private List<String> favoritedByClientNames;
}
