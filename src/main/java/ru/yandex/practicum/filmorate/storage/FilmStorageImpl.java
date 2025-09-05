package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.storage.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

@Deprecated
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

    @Override
    public void addLike(Long filmId, Long userId) {

    }

    @Override
    public void removeLike(Long filmId, Long userId) {

    }
}