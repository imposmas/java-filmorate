package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;
    private final UserService userService;
    public static final Comparator<Film> BY_LIKES_DESC =
            Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed()
                    .thenComparing(Film::getId);

    public FilmService(FilmStorage filmStorage, FilmValidator filmValidator, UserService userService) {
        this.filmStorage = filmStorage;
        this.filmValidator = filmValidator;
        this.userService = userService;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public Film create(Film film) {
        log.debug("createFilm parameters: name {}, description {}, releaseDate {}, duration {}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        checkFilmDuplicates(film);
        filmValidator.validate(film);
        return filmStorage.save(film);
    }

    public Film update(Film newFilm) {
        log.debug("updateFilm parameters: id {}, name {}, description {}, releaseDate {}, duration {}",
                newFilm.getId(), newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
                newFilm.getDuration());
        if (!filmStorage.existsById(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        checkFilmDuplicates(newFilm);
        return filmStorage.update(newFilm);
    }

    public void addLike(Long filmId, Long userId) {
        log.debug("addLike parameters: userId {}, film {}", userId, filmId);
        userService.findById(userId);
        findById(filmId).getLikes().add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.debug("removeLike parameters: userId {}, film {}", userId, filmId);
        userService.findById(userId);
        findById(filmId).getLikes().remove(userId);
        log.debug("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(BY_LIKES_DESC)
                .limit(count)
                .collect(Collectors.toList());
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
