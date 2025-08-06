package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class InMemoryStorage<T> {
    protected final Map<Long, T> storage = new HashMap<>();
    private long currentId = 0;

    public Collection<T> findAll() {
        return storage.values();
    }

    public T findById(Long id) {
        return storage.get(id);
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
