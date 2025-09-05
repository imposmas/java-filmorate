package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

@Repository("filmDbStorage")
public class FilmDbStorage extends AbstractDbStorage<Film> implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> findAll() {
        var films = jdbcTemplate.query(
                "SELECT * FROM FILMS",
                new FilmRowMapper()
        );

        films.forEach(film -> {
            loadGenres(film);
            loadLikes(film);
        });

        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        var filmOpt = jdbcTemplate.query(
                "SELECT * FROM FILMS WHERE ID = ?",
                new FilmRowMapper(),
                id
        ).stream().findFirst();

        filmOpt.ifPresent(film -> {
            loadGenres(film);
            loadLikes(film);
        });

        return filmOpt;
    }

    @Override
    public Film save(Film film) {
        String sql = "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate()); // null-safe
            ps.setInt(4, film.getDuration());
            if (film.getMpaRating() != null) {
                ps.setLong(5, film.getMpaRating());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        saveGenres(film);

        return film;
    }

    @Override
    public Film update(Film film) {
        if (!existsById(film.getId())) {
            throw new IllegalArgumentException("Film not found with id " + film.getId());
        }

        String sql = "UPDATE FILMS SET NAME=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, MPA_RATING=? WHERE ID=?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRating(),
                film.getId()
        );

        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE FILM_ID=?", film.getId());
        saveGenres(film);

        return film;
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FILMS WHERE ID=?", Integer.class, id);
        return count != null && count > 0;
    }

    // ------------------------------- Genres --------------------------------

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        String sql = "INSERT INTO FILM_GENRES (FILM_ID, GENRE_ID) VALUES (?, ?)";
        film.getGenres().forEach(genreId -> jdbcTemplate.update(sql, film.getId(), genreId));
    }

    private void loadGenres(Film film) {
        var genreIds = jdbcTemplate.query(
                "SELECT GENRE_ID FROM FILM_GENRES WHERE FILM_ID = ?",
                (rs, rowNum) -> rs.getLong("GENRE_ID"),
                film.getId()
        );
        film.setGenres(new HashSet<>(genreIds));
    }

    // ---------------------------------- Likes  ------------------------------

    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM FILM_LIKES WHERE FILM_ID=? AND USER_ID=?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private void loadLikes(Film film) {
        var likeIds = jdbcTemplate.query(
                "SELECT USER_ID FROM FILM_LIKES WHERE FILM_ID = ?",
                (rs, rowNum) -> rs.getLong("USER_ID"),
                film.getId()
        );
        film.setLikes(new HashSet<>(likeIds));
    }
}