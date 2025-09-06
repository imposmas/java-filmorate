package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.FilmStorage;
import ru.yandex.practicum.filmorate.dal.storage.GenreStorage;
import ru.yandex.practicum.filmorate.dal.storage.MpaRatingStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.validator.FilmValidator;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;
    private final UserService userService;
    private final FilmMapper filmMapper;
    private final MpaRatingStorage mpaRatingStorage;
    private final GenreStorage genreStorage;

    /**
     * Компаратор для сортировки фильмов по количеству лайков (убывание),
     * при равном количестве лайков используется ID фильма для стабилизации сортировки.
     */
    public static final Comparator<Film> BY_LIKES_DESC =
            Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed()
                    .thenComparing(Film::getId);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       MpaRatingStorage mpaRatingStorage, GenreStorage genreStorage,
                       FilmValidator filmValidator, UserService userService, FilmMapper filmMapper) {
        this.filmStorage = filmStorage;
        this.filmValidator = filmValidator;
        this.userService = userService;
        this.filmMapper = filmMapper;
        this.mpaRatingStorage = mpaRatingStorage;
        this.genreStorage = genreStorage;
    }

    // ---------------------------------- Films ------------------------------------------

    /**
     * Получение всех фильмов.
     * Каждый фильм преобразуется в DTO с полными данными о MPA и жанрах.
     *
     * @return коллекция FilmDto всех фильмов
     */
    public Collection<FilmDto> findAll() {
        return filmStorage.findAll().stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение фильма по ID.
     *
     * @param id идентификатор фильма
     * @return FilmDto с полными данными
     * @throws NotFoundException если фильм не найден
     */
    public FilmDto findById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Film with id = " + id + " not found"));
        return filmMapper.toDto(film);
    }

    /**
     * Создание нового фильма.
     * Проводится валидация MPA, жанров и основных данных фильма.
     *
     * @param request данные нового фильма
     * @return FilmDto созданного фильма
     * @throws NotFoundException если MPA или жанр не найден
     */
    public FilmDto create(NewFilmRequest request) {
        Film film = filmMapper.toEntity(request);
        validateMpaExists(film.getMpaRating());
        validateGenres(film.getGenres());
        filmValidator.validate(film);

        Film savedFilm = filmStorage.save(film);
        return filmMapper.toDto(savedFilm);
    }

    /**
     * Обновление существующего фильма.
     * Проводится валидация данных фильма и обновление записей в БД.
     *
     * @param request данные для обновления фильма
     * @return FilmDto обновленного фильма
     * @throws NotFoundException если фильм не найден
     */
    public FilmDto update(UpdateFilmRequest request) {
        Film existingFilm = filmStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("Film with id = " + request.getId() + " not found"));

        filmMapper.updateEntity(existingFilm, request);
        filmValidator.validate(existingFilm);
        Film updatedFilm = filmStorage.update(existingFilm);
        return filmMapper.toDto(updatedFilm);
    }

    /**
     * Получение популярных фильмов.
     * Сортировка по количеству лайков (убывание), ограничение количеством.
     *
     * @param count количество фильмов для получения
     * @return список популярных FilmDto
     */
    public List<FilmDto> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(BY_LIKES_DESC)
                .limit(count)
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    // ------------------------ Валидация -----------------------------------

    /**
     * Проверяет, существует ли MPA с указанным ID.
     *
     * @param mpa идентификатор MPA
     * @throws NotFoundException если MPA не найден
     */
    private void validateMpaExists(MpaRating mpa) {
        if (mpa != null && !mpaRatingStorage.existsById(mpa.getId())) {
            throw new NotFoundException("MPA rating with id = " + mpa.getId() + " not found");
        }
    }

    /**
     * Проверяет существование всех жанров по их ID.
     *
     * @param genres множество ID жанров
     * @throws NotFoundException если какой-либо жанр не найден
     */

    private void validateGenres(Set<Genre> genres) {
        for (Genre genre : genres) {
            if (genre != null && !genreStorage.existsById(genre.getId())) {
                throw new NotFoundException("Genre with id = " + genre.getId() + " not found");
            }
        }
    }

    // ------------------------ Likes -----------------------------------

    /**
     * Добавление лайка фильму от пользователя.
     * Если пользователь уже поставил лайк, запись не дублируется.
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     * @throws NotFoundException если фильм или пользователь не найден
     */
    public void addLike(Long filmId, Long userId) {
        userService.findById(userId);
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id = " + filmId + " not found"));

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        if (film.getLikes().add(userId)) {
            filmStorage.addLike(filmId, userId); // запись в БД
        }
    }

    /**
     * Удаление лайка фильма от пользователя.
     * Если лайка нет — операция игнорируется.
     *
     * @param filmId идентификатор фильма
     * @param userId идентификатор пользователя
     * @throws NotFoundException если фильм или пользователь не найден
     */
    public void removeLike(Long filmId, Long userId) {
        userService.findById(userId);
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id = " + filmId + " not found"));

        if (film.getLikes() != null && film.getLikes().remove(userId)) {
            filmStorage.removeLike(filmId, userId);
        }
    }

    /**
     * Проверка на дубликаты фильмов по имени.
     *
     * @deprecated Используется в старой логике, рекомендуется использовать уникальность имени на уровне БД
     */
    @Deprecated
    private void checkFilmDuplicates(Film film) {
        boolean duplicate = filmStorage.findAll().stream()
                .anyMatch(existing ->
                        !existing.getId().equals(film.getId()) &&
                                existing.getName().equals(film.getName())
                );
        if (duplicate) {
            throw new DuplicatedDataException("Film with this name already exists");
        }
    }
}