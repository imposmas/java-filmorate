package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

@Repository
public class FilmStorageImpl extends InMemoryStorage<Film> implements FilmStorage {
    @Override
    protected void setId(Film film, Long id) {
        film.setId(id);
    }

    @Override
    protected Long getId(Film film) {
        return film.getId();
    }
}