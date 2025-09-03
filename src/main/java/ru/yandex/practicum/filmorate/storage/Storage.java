package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.Optional;

public interface Storage<T> {
    Collection<T> findAll();

    Optional<T> findById(Long id);

    T save(T entity);

    T update(T entity);

    boolean existsById(Long id);
}