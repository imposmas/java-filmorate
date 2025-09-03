package ru.yandex.practicum.filmorate.storage;

import java.util.*;

public abstract class InMemoryStorage<T> implements Storage<T> {
    protected final Map<Long, T> storage = new HashMap<>();
    private long currentId = 0;

    public Collection<T> findAll() {
        return storage.values();
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public T save(T entity) {
        Long id = getNextId();
        setId(entity, id);
        storage.put(id, entity);
        return entity;
    }

    public T update(T entity) {
        Long id = getId(entity);
        if (!storage.containsKey(id)) {
            throw new NoSuchElementException("Entity with id = " + id + " not found.");
        }
        storage.put(id, entity);
        return entity;
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public Collection<T> getAll() {
        return storage.values();
    }

    protected Long getNextId() {
        return ++currentId;
    }

    protected abstract void setId(T entity, Long id);

    protected abstract Long getId(T entity);
}
