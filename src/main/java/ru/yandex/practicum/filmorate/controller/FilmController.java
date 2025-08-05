package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    /**
     * films map.
     */
    private final Map<Long, Film> films = new HashMap<>();

    /**
     * @return all files from the map
     */
    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    /**
     * @param film
     * @return added film
     */
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.debug("createFilm parameters: name {}, description {}, releaseDate {}, duration {}:  ",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        checkFilmDuplicates(film);
        FilmValidator filmParametersValidator = new FilmValidator();
        filmParametersValidator.validate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    /**
     * @param newFilm
     * @return updated film
     */
    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.debug("createFilm parameters: id {}, name {}, description {}, releaseDate {}, duration {}:  ",
                newFilm.getId(), newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration());
        if (isFilmExistsById(newFilm)) {
            checkFilmDuplicates(newFilm);
            films.put(newFilm.getId(), newFilm);
            return newFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkFilmDuplicates(Film film) {
        boolean duplicate = films.values().stream()
                .anyMatch(existing ->
                        !existing.getId().equals(film.getId()) &&
                                existing.getName().equals(film.getName())
                );

        if (duplicate) {
            throw new DuplicatedDataException("Фильм с таким названием уже существует");
        }
    }

    private boolean isFilmExistsById(Film film) {
        return films.containsKey(film.getId());
    }
}