package com.domain.webflux.service.repository;

import java.util.Optional;

public interface Database {

    <T> T save(final String key, final T value);

    <T> Optional<T> get(final String key, final Class<T> type);
}
