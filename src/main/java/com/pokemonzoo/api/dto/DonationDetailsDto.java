package com.pokemonzoo.api.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DonationDetailsDto {
    private UUID donationId;
    private String clientName;
    private List<String> habitatNames;
    private String type;
    private Double amount;
}
