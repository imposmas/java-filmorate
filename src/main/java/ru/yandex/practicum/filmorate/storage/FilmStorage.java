package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

public class FilmStorage extends InMemoryStorage<Film> {
    @Override
    protected void setId(Film film, Long id) {
        film.setId(id);
    }

    @Override
    protected Long getId(Film film) {
        return film.getId();
    }
}