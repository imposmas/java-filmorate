package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository("filmDbStorage")
public class FilmDbStorage extends AbstractDbStorage<Film> implements FilmStorage {

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "FILMS", "ID", new FilmRowMapper());
    }

    @Override
    public Film save(Film film) {
        Long id = insertAndReturnId(
                "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING) VALUES (?, ?, ?, ?, ?)",
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpaRating().getId()
        );
        film.setId(id);
        saveGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!existsById(film.getId())) {
            throw new IllegalArgumentException("Film not found with id " + film.getId());
        }

        jdbcTemplate.update(
                "UPDATE FILMS SET NAME=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, MPA_RATING=? WHERE ID=?",
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpaRating().getId(), film.getId()
        );

        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE FILM_ID=?", film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, " +
                "m.ID AS MPA_ID, m.NAME AS MPA_NAME " +
                "FROM FILMS f " +
                "LEFT JOIN MPA_RATINGS m ON f.MPA_RATING = m.ID";
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());

        films.forEach(f -> {
            loadGenres(f);
            loadLikes(f);
        });

        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, " +
                "m.ID AS MPA_ID, m.NAME AS MPA_NAME " +
                "FROM FILMS f " +
                "LEFT JOIN MPA_RATINGS m ON f.MPA_RATING = m.ID " +
                "WHERE f.ID = ?";
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);
        if (films.isEmpty()) return Optional.empty();

        Film film = films.get(0);
        loadGenres(film);
        loadLikes(film);
        return Optional.of(film);
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

        jdbcTemplate.batchUpdate(sql, film.getGenres(), film.getGenres().size(),
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genre.getId());
                }
        );
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbcTemplate.query(
                "SELECT g.ID, g.NAME FROM GENRES g " +
                        "JOIN FILM_GENRES fg ON g.ID = fg.GENRE_ID " +
                        "WHERE fg.FILM_ID = ?",
                (rs, rowNum) -> new Genre(rs.getLong("ID"), rs.getString("NAME")),
                film.getId()
        );
        film.setGenres(new HashSet<>(genres));
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