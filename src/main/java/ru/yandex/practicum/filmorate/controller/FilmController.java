package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage = new FilmStorage();

    @GetMapping
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.debug("createFilm parameters: name {}, description {}, releaseDate {}, duration {}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        checkFilmDuplicates(film);
        new FilmValidator().validate(film);
        return filmStorage.save(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.debug("updateFilm parameters: id {}, name {}, description {}, releaseDate {}, duration {}",
                newFilm.getId(), newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration());
        if (!filmStorage.existsById(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        checkFilmDuplicates(newFilm);
        return filmStorage.update(newFilm);
    }

    private void checkFilmDuplicates(Film film) {
        boolean duplicate = filmStorage.findAll().stream()
                .anyMatch(existing ->
                        !existing.getId().equals(film.getId()) &&
                                existing.getName().equals(film.getName())
                );

        if (duplicate) {
            throw new DuplicatedDataException("Фильм с таким названием уже существует");
        }
    }
}