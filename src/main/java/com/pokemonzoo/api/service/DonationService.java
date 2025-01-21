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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;
    private final AppClientRepository appClientRepository;
    private final HabitatRepository habitatRepository;
    private final DonationHabitatRepository donationHabitatRepository;

    private DonationDetailsDto mapDonationToDto(Donation donation) {
        AppClient client = donation.getClient();

        List<String> habitatNames = donation.getHabitatDonations().stream()
                .map(DonationHabitat::getHabitat)
                .map(Habitat::getName)
                .toList();

        DonationDetailsDto donationDetailsDto = new DonationDetailsDto();
        donationDetailsDto.setClientName(client.getUsername());
        donationDetailsDto.setHabitatNames(habitatNames);
        donationDetailsDto.setType(donation.getType());
        donationDetailsDto.setAmount(donation.getAmount());

        return donationDetailsDto;
    }

    private List<DonationDetailsDto> mapDonationsToDto(List<Donation> donations) {
        return donations.stream().map(this::mapDonationToDto).toList();
    }

    public List<DonationDetailsDto> getDonations() {
        List<Donation> donations = donationRepository.findAll();
        return mapDonationsToDto(donations);
    }
}
