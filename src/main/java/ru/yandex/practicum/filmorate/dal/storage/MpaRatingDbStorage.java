package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaRatingDbStorage extends AbstractDbStorage<MpaRating> implements MpaRatingStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper rowMapper = new MpaRatingRowMapper();

    public MpaRatingDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<MpaRating> findAll() {
        String sql = "SELECT * FROM MPA_RATINGS ORDER BY ID";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<MpaRating> findById(Long id) {
        String sql = "SELECT * FROM MPA_RATINGS WHERE ID = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    @Override
    public MpaRating save(MpaRating rating) {
        String sql = "INSERT INTO MPA_RATINGS (NAME) VALUES (?)";
        jdbcTemplate.update(sql, rating.getName());

        // Получаем сгенерированный ID
        Long id = jdbcTemplate.queryForObject("SELECT ID FROM MPA_RATINGS WHERE NAME = ?", Long.class, rating.getName());
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

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM MPA_RATINGS WHERE ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
