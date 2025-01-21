package com.pokemonzoo.api.dto;

import com.pokemonzoo.api.model.Pokemon;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AppClientDetailsDto {
    private UUID clientId;
    private String username;
    private List<String> favoritePokemonList;
}