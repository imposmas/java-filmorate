package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

@Repository
public class MpaRatingDbStorage extends AbstractDbStorage<MpaRating> implements MpaRatingStorage {

    public MpaRatingDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "MPA_RATINGS", "ID", new MpaRatingRowMapper());
    }

    @Override
    public Collection<MpaRating> findAll() {
        String sql = "SELECT * FROM MPA_RATINGS ORDER BY ID";
        return jdbcTemplate.query(sql, new MpaRatingRowMapper());
    }

    @Override
    public MpaRating save(MpaRating rating) {
        String sql = "INSERT INTO MPA_RATINGS (NAME) VALUES (?)";
        Long id = insertAndReturnId(sql, rating.getName());
        rating.setId(id);
        return rating;
    }

    @Override
    public MpaRating update(MpaRating rating) {
        String sql = "UPDATE MPA_RATINGS SET NAME = ? WHERE ID = ?";
        int rows = jdbcTemplate.update(sql, rating.getName(), rating.getId());
        if (rows == 0) {
            throw new IllegalArgumentException("MPA Rating with id = " + rating.getId() + " not found");
        }
        return rating;
    }
}