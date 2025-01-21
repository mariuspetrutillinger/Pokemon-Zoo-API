package com.pokemonzoo.api;

import com.pokemonzoo.api.controller.DonationController;
import com.pokemonzoo.api.dto.DonationDetailsDto;
import com.pokemonzoo.api.service.DonationHabitatService;
import com.pokemonzoo.api.service.DonationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationControllerTest {

    @Mock
    private DonationService donationService;
    @Mock
    private DonationHabitatService donationHabitatService;

    private DonationController donationController;

    @BeforeEach
    void setUp() {
        donationController = new DonationController(donationService, donationHabitatService);
    }

    @Test
    void getAllDonations_WithValidRequest_ShouldReturnDonations() {
        // Arrange
        List<DonationDetailsDto> expectedDonations = Arrays.asList(
                createTestDonationDto("user1", "FOOD", 100.0),
                createTestDonationDto("user2", "SUPPLIES", 200.0)
        );
        when(donationService.getDonations()).thenReturn(expectedDonations);

        // Act
        ResponseEntity<List<DonationDetailsDto>> response = donationController.getAllDonations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedDonations, response.getBody());
    }

    @Test
    void getAllDonations_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(donationService.getDonations()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<DonationDetailsDto>> response = donationController.getAllDonations();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void makeDonation_WithValidRequest_ShouldCreateDonation() {
        // Arrange
        DonationDetailsDto donationDto = createTestDonationDto("user1", "FOOD", 100.0);
        doNothing().when(donationHabitatService).makeDonation(any(DonationDetailsDto.class));

        // Act
        ResponseEntity<String> response = donationController.makeDonation(donationDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Donation made successfully", response.getBody());
        verify(donationHabitatService).makeDonation(donationDto);
    }

    @Test
    void makeDonation_WithInvalidRequest_ShouldReturnBadRequest() {
        // Arrange
        DonationDetailsDto donationDto = createTestDonationDto("user1", "FOOD", 100.0);
        doThrow(new IllegalArgumentException("Invalid donation data"))
                .when(donationHabitatService).makeDonation(any(DonationDetailsDto.class));

        // Act
        ResponseEntity<String> response = donationController.makeDonation(donationDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void makeDonation_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        DonationDetailsDto donationDto = createTestDonationDto("user1", "FOOD", 100.0);
        doThrow(new RuntimeException("Database error"))
                .when(donationHabitatService).makeDonation(any(DonationDetailsDto.class));

        // Act
        ResponseEntity<String> response = donationController.makeDonation(donationDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void makeDonation_WithNullDto_ShouldReturnBadRequest() {
        // Arrange
        DonationDetailsDto donationDto = null;
        doThrow(new IllegalArgumentException("Invalid donation data"))
                .when(donationHabitatService).makeDonation(null);

        // Act
        ResponseEntity<String> response = donationController.makeDonation(donationDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    private DonationDetailsDto createTestDonationDto(String clientName, String type, Double amount) {
        DonationDetailsDto dto = new DonationDetailsDto();
        dto.setClientName(clientName);
        dto.setType(type);
        dto.setAmount(amount);
        dto.setHabitatNames(List.of("Forest", "Desert"));
        return dto;
    }
}