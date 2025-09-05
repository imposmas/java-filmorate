package ru.yandex.practicum.filmorate.dal.storage;


import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage extends Storage<Film> {
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}
