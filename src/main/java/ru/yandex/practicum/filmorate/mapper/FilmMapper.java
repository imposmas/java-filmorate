package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class FilmMapper {

    private final MpaRatingMapper mpaRatingMapper;
    private final GenreMapper genreMapper;

    private static final Comparator<GenreDto> GENRE_ID_COMPARATOR =
            Comparator.comparingLong(GenreDto::getId);

    public FilmMapper(MpaRatingMapper mpaRatingMapper, GenreMapper genreMapper) {
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
            dto.setMpaRating(mpaRatingMapper.toDto(film.getMpaRating()));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            dto.setGenres(film.getGenres().stream()
                    .map(genreMapper::toDto)
                    .sorted(GENRE_ID_COMPARATOR)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new)));
        }

        dto.setLikes(film.getLikes());
        return dto;
    }

    public Film toEntity(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpaRating(request.getMpaRating() != null ? new MpaRating(request.getMpaRating().getId(),
                request.getMpaRating().getName()) : null);
        if (request.getGenres() != null) {
            film.setGenres(request.getGenres().stream()
                    .map(g -> new Genre(g.getId(), g.getName()))
                    .collect(Collectors.toSet()));
        }
        film.setLikes(new HashSet<>());
        return film;
    }

    public void updateEntity(Film film, UpdateFilmRequest request) {
        if (request.getName() != null) film.setName(request.getName());
        if (request.getDescription() != null) film.setDescription(request.getDescription());
        if (request.getReleaseDate() != null) film.setReleaseDate(request.getReleaseDate());
        if (request.getDuration() != 0) film.setDuration(request.getDuration());
        if (request.getMpaRating() != null)
            film.setMpaRating(new MpaRating(request.getMpaRating().getId(), request.getMpaRating().getName()));
        if (request.getGenres() != null)
            film.setGenres(request.getGenres().stream()
                    .map(g -> new Genre(g.getId(), g.getName()))
                    .collect(Collectors.toSet()));
    }
}

