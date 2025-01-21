package com.pokemonzoo.api;

import com.pokemonzoo.api.dto.DonationDetailsDto;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.Donation;
import com.pokemonzoo.api.model.DonationHabitat;
import com.pokemonzoo.api.model.Habitat;
import com.pokemonzoo.api.repository.AppClientRepository;
import com.pokemonzoo.api.repository.DonationHabitatRepository;
import com.pokemonzoo.api.repository.DonationRepository;
import com.pokemonzoo.api.repository.HabitatRepository;
import com.pokemonzoo.api.service.DonationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;
    @Mock
    private AppClientRepository appClientRepository;
    @Mock
    private HabitatRepository habitatRepository;
    @Mock
    private DonationHabitatRepository donationHabitatRepository;

    private DonationService donationService;

    @BeforeEach
    void setUp() {
        donationService = new DonationService(
                donationRepository,
                appClientRepository,
                habitatRepository,
                donationHabitatRepository
        );
    }

    @Test
    void getDonations_ShouldReturnMappedDonations() {
        // Arrange
        AppClient client = new AppClient();
        client.setUsername("testUser");

        Habitat habitat1 = new Habitat();
        habitat1.setName("Forest");
        Habitat habitat2 = new Habitat();
        habitat2.setName("Desert");

        DonationHabitat donationHabitat1 = new DonationHabitat();
        donationHabitat1.setHabitat(habitat1);
        DonationHabitat donationHabitat2 = new DonationHabitat();
        donationHabitat2.setHabitat(habitat2);

        Set<DonationHabitat> habitatDonations = new HashSet<>();
        habitatDonations.add(donationHabitat1);
        habitatDonations.add(donationHabitat2);

        Donation donation1 = new Donation();
        donation1.setClient(client);
        donation1.setType("FOOD");
        donation1.setAmount(100.0);
        donation1.setHabitatDonations(habitatDonations);

        List<Donation> donations = List.of(donation1);
        when(donationRepository.findAll()).thenReturn(donations);

        // Act
        List<DonationDetailsDto> result = donationService.getDonations();

        // Assert
        assertEquals(1, result.size());
        DonationDetailsDto dto = result.get(0);
        assertEquals("testUser", dto.getClientName());
        assertEquals("FOOD", dto.getType());
        assertEquals(100.0, dto.getAmount());
        assertEquals(2, dto.getHabitatNames().size());
        assertTrue(dto.getHabitatNames().contains("Forest"));
        assertTrue(dto.getHabitatNames().contains("Desert"));
    }

    @Test
    void getDonations_WithEmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(donationRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<DonationDetailsDto> result = donationService.getDonations();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getDonations_WithMultipleDonations_ShouldMapAllDonations() {
        // Arrange
        AppClient client1 = new AppClient();
        client1.setUsername("user1");
        AppClient client2 = new AppClient();
        client2.setUsername("user2");

        Habitat habitat = new Habitat();
        habitat.setName("Forest");

        DonationHabitat donationHabitat1 = new DonationHabitat();
        donationHabitat1.setHabitat(habitat);
        DonationHabitat donationHabitat2 = new DonationHabitat();
        donationHabitat2.setHabitat(habitat);

        Donation donation1 = new Donation();
        donation1.setClient(client1);
        donation1.setType("FOOD");
        donation1.setAmount(100.0);
        donation1.setHabitatDonations(Set.of(donationHabitat1));

        Donation donation2 = new Donation();
        donation2.setClient(client2);
        donation2.setType("SUPPLIES");
        donation2.setAmount(200.0);
        donation2.setHabitatDonations(Set.of(donationHabitat2));

        List<Donation> donations = List.of(donation1, donation2);
        when(donationRepository.findAll()).thenReturn(donations);

        // Act
        List<DonationDetailsDto> result = donationService.getDonations();

        // Assert
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getClientName());
        assertEquals("user2", result.get(1).getClientName());
        assertEquals(100.0, result.get(0).getAmount());
        assertEquals(200.0, result.get(1).getAmount());
        assertEquals("FOOD", result.get(0).getType());
        assertEquals("SUPPLIES", result.get(1).getType());
    }

    @Test
    void getDonations_WithNullClient_ShouldHandleNullClient() {
        // Arrange
        Habitat habitat = new Habitat();
        habitat.setName("Forest");

        DonationHabitat donationHabitat = new DonationHabitat();
        donationHabitat.setHabitat(habitat);

        Donation donation = new Donation();
        donation.setClient(null); // Null client for anonymous donation
        donation.setType("FOOD");
        donation.setAmount(100.0);
        donation.setHabitatDonations(Set.of(donationHabitat));

        List<Donation> donations = List.of(donation);
        when(donationRepository.findAll()).thenReturn(donations);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> donationService.getDonations());
    }

    @Test
    void mapDonationToDto_WithEmptyHabitatDonations_ShouldReturnEmptyHabitatList() {
        // Arrange
        AppClient client = new AppClient();
        client.setUsername("testUser");

        Donation donation = new Donation();
        donation.setClient(client);
        donation.setType("FOOD");
        donation.setAmount(100.0);
        donation.setHabitatDonations(Collections.emptySet());

        List<Donation> donations = List.of(donation);
        when(donationRepository.findAll()).thenReturn(donations);

        // Act
        List<DonationDetailsDto> result = donationService.getDonations();

        // Assert
        assertEquals(1, result.size());
        DonationDetailsDto dto = result.get(0);
        assertTrue(dto.getHabitatNames().isEmpty());
        assertEquals("testUser", dto.getClientName());
        assertEquals("FOOD", dto.getType());
        assertEquals(100.0, dto.getAmount());
    }
}
