package ru.yandex.practicum.filmorate.dal.storage;

import java.util.Collection;
import java.util.Optional;

public abstract class AbstractDbStorage<T> implements Storage<T> {

    public abstract Collection<T> findAll();

    public abstract Optional<T> findById(Long id);

    public abstract T save(T entity);

    public abstract T update(T entity);

    public abstract boolean existsById(Long id);
}
