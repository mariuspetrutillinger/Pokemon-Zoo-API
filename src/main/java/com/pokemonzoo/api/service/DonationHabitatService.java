package com.pokemonzoo.api.service;

import com.pokemonzoo.api.dto.DonationDetailsDto;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.model.Donation;
import com.pokemonzoo.api.model.DonationHabitat;
import com.pokemonzoo.api.model.Habitat;
import com.pokemonzoo.api.repository.AppClientRepository;
import com.pokemonzoo.api.repository.DonationHabitatRepository;
import com.pokemonzoo.api.repository.DonationRepository;
import com.pokemonzoo.api.repository.HabitatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DonationHabitatService {
    private final DonationHabitatRepository donationHabitatRepository;
    private final DonationRepository donationRepository;
    private final HabitatRepository habitatRepository;
    private final AppClientRepository clientRepository;

    @Transactional
    public void makeDonation(DonationDetailsDto donationDetailsDto) {
        Donation donation = new Donation();
        donation.setType(donationDetailsDto.getType());
        donation.setAmount(donationDetailsDto.getAmount());

        if (donationDetailsDto.getClientName() != null) {
            AppClient client = clientRepository.findByUsername(donationDetailsDto.getClientName())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found with name: " + donationDetailsDto.getClientName()));
            donation.setClient(client);
        }

        // Save the donation first to get its ID
        donation = donationRepository.save(donation);

        // Update the habitat donations in place
        updateOrCreateDonationHabitats(donationDetailsDto, donation);

        // Save the donation again to persist the associations
        donationRepository.save(donation);
    }

    private void updateOrCreateDonationHabitats(DonationDetailsDto donationDetailsDto, Donation donation) {
        List<Habitat> habitats = getHabitatList(donationDetailsDto);

        Double allocatedAmount = donationDetailsDto.getAmount() / habitats.size();

        // Get the existing collection
        Set<DonationHabitat> existingHabitats = donation.getHabitatDonations();
        if (existingHabitats == null) {
            donation.setHabitatDonations(new HashSet<>());
            existingHabitats = donation.getHabitatDonations();
        }

        for (Habitat habitat : habitats) {
            // Find an existing association
            DonationHabitat existingDonationHabitat = existingHabitats.stream()
                    .filter(dh -> dh.getHabitat().equals(habitat))
                    .findFirst()
                    .orElse(null);

            if (existingDonationHabitat != null) {
                // Update the allocation amount
                existingDonationHabitat.setAllocationAmount(existingDonationHabitat.getAllocationAmount() + allocatedAmount);
            } else {
                // Create a new association
                DonationHabitat donationHabitat = new DonationHabitat();
                donationHabitat.setDonation(donation);
                donationHabitat.setHabitat(habitat);
                donationHabitat.setAllocationAmount(allocatedAmount);

                existingHabitats.add(donationHabitat); // Add to the existing collection
                habitat.getHabitatDonations().add(donationHabitat); // Maintain bidirectional relationship
            }

            // Update the habitat's food supply
            habitat.setFoodSupply(habitat.getFoodSupply() != null
                    ? habitat.getFoodSupply() + allocatedAmount
                    : allocatedAmount);
        }

        // Save updated habitats
        habitatRepository.saveAll(habitats);
    }

    private List<Habitat> getHabitatList(DonationDetailsDto donationDetailsDto) {
        List<String> habitatNames = donationDetailsDto.getHabitatNames();
        if (habitatNames == null || habitatNames.isEmpty()) {
            throw new IllegalArgumentException("Habitat names must not be null or empty");
        }

        // Deduplicate habitat names using a Set
        Set<String> uniqueHabitatNames = new HashSet<>(habitatNames);

        List<Habitat> habitats = new ArrayList<>();
        for (String habitatName : uniqueHabitatNames) {  // Iterate over unique names
            Habitat habitat = habitatRepository.findByName(habitatName)
                    .orElseThrow(() -> new EntityNotFoundException("Habitat not found with name: " + habitatName));
            habitats.add(habitat);
        }

        return habitats;
    }
}