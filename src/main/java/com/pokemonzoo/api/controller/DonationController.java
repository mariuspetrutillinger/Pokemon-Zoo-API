package com.pokemonzoo.api.controller;

import com.pokemonzoo.api.dto.DonationDetailsDto;
import com.pokemonzoo.api.service.DonationHabitatService;
import com.pokemonzoo.api.service.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/donations")
@Tag(name = "Donation", description = "Endpoints for managing donations")
public class DonationController {
    private final DonationService donationService;
    private final DonationHabitatService donationHabitatService;

    @Operation(summary = "Get all donations",
            description = "Get all donations made")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donations retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<List<DonationDetailsDto>> getAllDonations() {
        try {
            return ResponseEntity.ok(donationService.getDonations());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Make a donation",
            description = "Make a donation (donation id will be ignored)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donation made successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to make donation"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/add")
    public ResponseEntity<String> makeDonation(@RequestBody DonationDetailsDto donationDetailsDto) {
        try {
            donationHabitatService.makeDonation(donationDetailsDto);
            return ResponseEntity.ok("Donation made successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
