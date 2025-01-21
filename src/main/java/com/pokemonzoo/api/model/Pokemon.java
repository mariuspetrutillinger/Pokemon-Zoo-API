package com.pokemonzoo.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pokemon")
public class Pokemon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String name;

    @Column
    private String species;

    @Column
    private Integer age;

    @Column
    private Integer weight;

    @Column
    private Integer height;

    @ToString.Exclude
    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClientFavoritePokemon> favoritedByClients = new HashSet<>();

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "habitat_id")
    private Habitat habitat;
}

