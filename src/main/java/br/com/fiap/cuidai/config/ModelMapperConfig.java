package br.com.fiap.cuidai.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disponibiliza um ModelMapper para converter DTO <-> model.
 * STRICT: so copia atributos com o mesmo nome exato. Evita que, por exemplo,
 * "cidadaoId" do DTO seja copiado por engano para "id" da consulta.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
}
