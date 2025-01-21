package com.pokemonzoo.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class PokemonZooApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokemonZooApiApplication.class, args);
	}

}
