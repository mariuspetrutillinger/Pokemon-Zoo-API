package com.pokemonzoo.api.repository;

import com.pokemonzoo.api.model.Donation;
import com.pokemonzoo.api.model.DonationHabitat;
import com.pokemonzoo.api.model.Habitat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationHabitatRepository extends JpaRepository<DonationHabitat, UUID> {
    List<DonationHabitat> findByDonationId(UUID donationId);
    List<DonationHabitat> findByHabitatId(UUID habitatId);
    void deleteByDonationIdAndHabitatId(UUID donationId, UUID habitatId);

    @Query("SELECT dh FROM DonationHabitat dh WHERE dh.donation = :donation AND dh.habitat = :habitat")
    Optional<DonationHabitat> findByDonationAndHabitat(@Param("donation") Donation donation, @Param("habitat") Habitat habitat);

}
