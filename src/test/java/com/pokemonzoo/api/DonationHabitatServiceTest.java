package com.pokemonzoo.api;

import com.pokemonzoo.api.dto.DonationDetailsDto;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.Donation;
import com.pokemonzoo.api.model.Habitat;
import com.pokemonzoo.api.repository.AppClientRepository;
import com.pokemonzoo.api.repository.DonationHabitatRepository;
import com.pokemonzoo.api.repository.DonationRepository;
import com.pokemonzoo.api.repository.HabitatRepository;
import com.pokemonzoo.api.service.DonationHabitatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationHabitatServiceTest {

    @Mock
    private DonationHabitatRepository donationHabitatRepository;
    @Mock
    private DonationRepository donationRepository;
    @Mock
    private HabitatRepository habitatRepository;
    @Mock
    private AppClientRepository clientRepository;

    @Captor
    private ArgumentCaptor<List<Habitat>> habitatsCaptor;

    private DonationHabitatService donationHabitatService;

    @BeforeEach
    void setUp() {
        donationHabitatService = new DonationHabitatService(
                donationHabitatRepository,
                donationRepository,
                habitatRepository,
                clientRepository
        );
    }

    @Test
    void makeDonation_WithValidClientAndHabitats_ShouldCreateDonation() {
        // Arrange
        DonationDetailsDto dto = new DonationDetailsDto();
        dto.setClientName("testUser");
        dto.setType("FOOD");
        dto.setAmount(100.0);
        dto.setHabitatNames(List.of("Habitat1", "Habitat2"));

        AppClient client = new AppClient();
        client.setUsername("testUser");

        Habitat habitat1 = new Habitat();
        habitat1.setName("Habitat1");
        habitat1.setFoodSupply(50.0);
        habitat1.setHabitatDonations(new HashSet<>());

        Habitat habitat2 = new Habitat();
        habitat2.setName("Habitat2");
        habitat2.setFoodSupply(30.0);
        habitat2.setHabitatDonations(new HashSet<>());

        Donation savedDonation = new Donation();
        savedDonation.setId(UUID.randomUUID());
        savedDonation.setHabitatDonations(new HashSet<>());

        when(clientRepository.findByUsername("testUser")).thenReturn(Optional.of(client));
        when(habitatRepository.findByName("Habitat1")).thenReturn(Optional.of(habitat1));
        when(habitatRepository.findByName("Habitat2")).thenReturn(Optional.of(habitat2));
        when(donationRepository.save(any(Donation.class))).thenReturn(savedDonation);

        // Act
        donationHabitatService.makeDonation(dto);

        // Assert
        verify(donationRepository, times(2)).save(any(Donation.class));
        verify(habitatRepository).saveAll(habitatsCaptor.capture());

        List<Habitat> savedHabitats = habitatsCaptor.getValue();
        assertEquals(2, savedHabitats.size());
        assertEquals(100.0, savedHabitats.get(0).getFoodSupply() + savedHabitats.get(1).getFoodSupply() - 80.0, 0.01);
    }

    @Test
    void makeDonation_WithInvalidClient_ShouldThrowException() {
        // Arrange
        DonationDetailsDto dto = new DonationDetailsDto();
        dto.setClientName("nonexistentUser");
        dto.setHabitatNames(List.of("Habitat1"));

        when(clientRepository.findByUsername("nonexistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> donationHabitatService.makeDonation(dto));
        verify(donationRepository, never()).save(any());
    }

    @Test
    void makeDonation_WithInvalidHabitat_ShouldThrowException() {
        // Arrange
        DonationDetailsDto dto = new DonationDetailsDto();
        dto.setClientName("testUser");
        dto.setHabitatNames(List.of("NonexistentHabitat"));

        AppClient client = new AppClient();
        when(clientRepository.findByUsername("testUser")).thenReturn(Optional.of(client));
        when(habitatRepository.findByName("NonexistentHabitat")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> donationHabitatService.makeDonation(dto));
    }

    @Test
    void makeDonation_WithEmptyHabitatNames_ShouldThrowException() {
        // Arrange
        DonationDetailsDto dto = new DonationDetailsDto();
        dto.setClientName("testUser");
        dto.setHabitatNames(Collections.emptyList());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> donationHabitatService.makeDonation(dto));
    }

    @Test
    void makeDonation_WithDuplicateHabitats_ShouldHandleDuplicates() {
        // Arrange
        DonationDetailsDto dto = new DonationDetailsDto();
        dto.setClientName("testUser");
        dto.setType("FOOD");
        dto.setAmount(100.0);
        dto.setHabitatNames(List.of("Habitat1", "Habitat1")); // Duplicate habitat

        AppClient client = new AppClient();
        Habitat habitat = new Habitat();
        habitat.setName("Habitat1");
        habitat.setFoodSupply(50.0);
        habitat.setHabitatDonations(new HashSet<>());

        Donation savedDonation = new Donation();
        savedDonation.setId(UUID.randomUUID());
        savedDonation.setHabitatDonations(new HashSet<>());

        when(clientRepository.findByUsername("testUser")).thenReturn(Optional.of(client));
        when(habitatRepository.findByName("Habitat1")).thenReturn(Optional.of(habitat));
        when(donationRepository.save(any(Donation.class))).thenReturn(savedDonation);

        // Act
        donationHabitatService.makeDonation(dto);

        // Assert
        verify(habitatRepository).saveAll(habitatsCaptor.capture());
        List<Habitat> savedHabitats = habitatsCaptor.getValue();
        assertEquals(1, savedHabitats.size()); // Should only save once despite duplicate in input
        assertEquals(150.0, savedHabitats.get(0).getFoodSupply(), 0.01);
    }

    @Test
    void makeDonation_WithoutClient_ShouldCreateAnonymousDonation() {
        // Arrange
        DonationDetailsDto dto = new DonationDetailsDto();
        dto.setType("FOOD");
        dto.setAmount(100.0);
        dto.setHabitatNames(List.of("Habitat1"));

        Habitat habitat = new Habitat();
        habitat.setName("Habitat1");
        habitat.setFoodSupply(50.0);
        habitat.setHabitatDonations(new HashSet<>());

        Donation savedDonation = new Donation();
        savedDonation.setId(UUID.randomUUID());
        savedDonation.setHabitatDonations(new HashSet<>());

        when(habitatRepository.findByName("Habitat1")).thenReturn(Optional.of(habitat));
        when(donationRepository.save(any(Donation.class))).thenReturn(savedDonation);

        // Act
        donationHabitatService.makeDonation(dto);

        // Assert
        verify(clientRepository, never()).findByUsername(any());
        verify(donationRepository, times(2)).save(any(Donation.class));
        verify(habitatRepository).saveAll(habitatsCaptor.capture());

        List<Habitat> savedHabitats = habitatsCaptor.getValue();
        assertEquals(1, savedHabitats.size());
        assertEquals(150.0, savedHabitats.get(0).getFoodSupply(), 0.01);
    }

    @Test
    void makeDonation_WithNullFoodSupply_ShouldInitializeFoodSupply() {
        // Arrange
        DonationDetailsDto dto = new DonationDetailsDto();
        dto.setType("FOOD");
        dto.setAmount(100.0);
        dto.setHabitatNames(List.of("Habitat1"));

        Habitat habitat = new Habitat();
        habitat.setName("Habitat1");
        habitat.setFoodSupply(null); // Null initial food supply
        habitat.setHabitatDonations(new HashSet<>());

        Donation savedDonation = new Donation();
        savedDonation.setId(UUID.randomUUID());
        savedDonation.setHabitatDonations(new HashSet<>());

        when(habitatRepository.findByName("Habitat1")).thenReturn(Optional.of(habitat));
        when(donationRepository.save(any(Donation.class))).thenReturn(savedDonation);

        // Act
        donationHabitatService.makeDonation(dto);

        // Assert
        verify(habitatRepository).saveAll(habitatsCaptor.capture());
        List<Habitat> savedHabitats = habitatsCaptor.getValue();
        assertEquals(1, savedHabitats.size());
        assertEquals(100.0, savedHabitats.get(0).getFoodSupply(), 0.01);
    }
}