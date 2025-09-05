package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.storage.GenreStorage;
import ru.yandex.practicum.filmorate.dal.storage.MpaRatingStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilmMapper {

    private final MpaRatingStorage mpaStorage;
    private final MpaRatingMapper mpaRatingMapper;
    private final GenreStorage genreStorage;
    private final GenreMapper genreMapper;

    private static final Comparator<GenreDto> GENRE_ID_COMPARATOR =
            Comparator.comparingLong(GenreDto::getId);

    public FilmMapper(MpaRatingStorage mpaStorage, MpaRatingMapper mpaRatingMapper,
                      GenreStorage genreStorage, GenreMapper genreMapper) {
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.mpaRatingMapper = mpaRatingMapper;
        this.genreMapper = genreMapper;
    }

    public FilmDto toDto(Film film) {
        if (film == null) return null;

        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getMpaRating() != null) {
            MpaRating mpaRating = mpaStorage.findById(film.getMpaRating())
                    .orElseThrow(() -> new NotFoundException("MPA rating not found for id = " + film.getMpaRating()));
            dto.setMpaRating(mpaRatingMapper.toDto(mpaRating));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> genres = film.getGenres().stream()
                    .map(id -> genreStorage.findById(id)
                            .orElseThrow(() -> new NotFoundException("Genre not found for id = " + id)))
                    .collect(Collectors.toSet());
            dto.setGenres(genres.stream().map(genreMapper::toDto).sorted(GENRE_ID_COMPARATOR)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new)));
        }
        dto.setLikes(film.getLikes());

        return dto;
    }


    public Film toEntity(NewFilmRequest request) {
        if (request == null) return null;

        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpaRating(request.getMpaRating() != null ? request.getMpaRating().getId() : null);
        if (request.getGenres() != null) {
            film.setGenres(
                    request.getGenres().stream()
                            .map(GenreDto::getId)
                            .collect(Collectors.toSet())
            );
        }
        // likes по умолчанию пустой набор
        film.setLikes(new java.util.HashSet<>());
        return film;
    }

    public void updateEntity(Film film, UpdateFilmRequest request) {
        if (request.getName() != null) film.setName(request.getName());
        if (request.getDescription() != null) film.setDescription(request.getDescription());
        if (request.getReleaseDate() != null) film.setReleaseDate(request.getReleaseDate());
        if (request.getDuration() != 0) film.setDuration(request.getDuration());
        if (request.getMpaRating() != null) film.setMpaRating(request.getMpaRating());
        if (request.getGenres() != null) film.setGenres(request.getGenres());
    }
}
