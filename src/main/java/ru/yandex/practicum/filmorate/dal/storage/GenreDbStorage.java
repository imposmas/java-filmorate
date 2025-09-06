package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Repository("genreDbStorage")
public class GenreDbStorage extends AbstractDbStorage<Genre> implements GenreStorage {

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "GENRES", "ID", new GenreRowMapper());
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM GENRES ORDER BY ID";
        return jdbcTemplate.query(sql, new GenreRowMapper());
    }

    @Override
    public Genre save(Genre genre) {
        Long id = insertAndReturnId(
                "INSERT INTO GENRES (NAME) VALUES (?)",
                genre.getName()
        );
        genre.setId(id);
        return genre;
    }

    @Override
    public Genre update(Genre genre) {
        if (!existsById(genre.getId())) {
            throw new IllegalArgumentException("Жанр с id = " + genre.getId() + " не найден");
        }

        jdbcTemplate.update(
                "UPDATE GENRES SET NAME = ? WHERE ID = ?",
                genre.getName(), genre.getId()
        );
        return genre;
    }
}