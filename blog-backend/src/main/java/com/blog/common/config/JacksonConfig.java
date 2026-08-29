package com.blog.common.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 时间序列化统一为 yyyy-MM-dd HH:mm:ss（LocalDate 为 yyyy-MM-dd）。 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .serializerByType(LocalDateTime.class, new com.fasterxml.jackson.databind.JsonSerializer<LocalDateTime>() {
                    @Override
                    public void serialize(LocalDateTime value, com.fasterxml.jackson.core.JsonGenerator gen,
                                          com.fasterxml.jackson.databind.SerializerProvider sp) throws java.io.IOException {
                        gen.writeString(value.format(DATETIME));
                    }
                })
                .serializerByType(LocalDate.class, new com.fasterxml.jackson.databind.JsonSerializer<LocalDate>() {
                    @Override
                    public void serialize(LocalDate value, com.fasterxml.jackson.core.JsonGenerator gen,
                                          com.fasterxml.jackson.databind.SerializerProvider sp) throws java.io.IOException {
                        gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    }
                });
    }
}
