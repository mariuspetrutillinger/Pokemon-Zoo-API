package com.pokemonzoo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppClientPokemonRequest {
    private UUID clientId;
    private Set<UUID> pokemonIds;
}
