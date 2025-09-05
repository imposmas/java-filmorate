package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage extends AbstractDbStorage<Genre> implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper rowMapper = new GenreRowMapper();

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM GENRES ORDER BY ID";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Genre> findById(Long id) {
        String sql = "SELECT * FROM GENRES WHERE ID = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    @Override
    public Genre save(Genre genre) {
        String sql = "INSERT INTO GENRES (NAME) VALUES (?)";
        jdbcTemplate.update(sql, genre.getName());

        // Получаем сгенерированный ID
        Long id = jdbcTemplate.queryForObject("SELECT ID FROM GENRES WHERE NAME = ?", Long.class, genre.getName());
        genre.setId(id);
        return genre;
    }

    @Override
    public Genre update(Genre genre) {
        String sql = "UPDATE GENRES SET NAME = ? WHERE ID = ?";
        int rows = jdbcTemplate.update(sql, genre.getName(), genre.getId());
        if (rows == 0) {
            throw new IllegalArgumentException("Жанр с id = " + genre.getId() + " не найден");
        }
        return genre;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM GENRES WHERE ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
