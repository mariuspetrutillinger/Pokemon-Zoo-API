package com.pokemonzoo.api.dto;

import com.pokemonzoo.api.service.enums.SearchType;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchPokemonDTO {
    SearchType searchType;

    String searchTerm;

    @Nullable
    Integer page;
}
