package com.domain.webflux.service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class InMemoryDatabase implements Database {

    public static final Map<String, String> DATABASE = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public <T> T save(String key, T value) {
        final var data = this.objectMapper.writeValueAsString(value);
        DATABASE.put(key, data);
        return value;

    }

    @Override
    public <T> Optional<T> get(String key, final Class<T> type) {
        final String json = DATABASE.get(key);
        return Optional.ofNullable(json)
                .map(data -> {
                    try {
                        return objectMapper.readValue(data, type);

                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
    }

}
