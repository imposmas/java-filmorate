package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

public abstract class AbstractDbStorage<T> implements Storage<T> {

    protected final JdbcTemplate jdbcTemplate;
    private final String tableName;
    private final String idColumn;
    private final RowMapper<T> rowMapper;

    protected AbstractDbStorage(JdbcTemplate jdbcTemplate, String tableName, String idColumn, RowMapper<T> rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.rowMapper = rowMapper;
    }

    @Override
    public Collection<T> findAll() {
        String sql = String.format("SELECT * FROM %s", tableName);
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<T> findById(Long id) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, idColumn);
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", tableName, idColumn);
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    /**
     * Вставка с возвратом сгенерированного ID.
     */
    protected Long insertAndReturnId(String sql, Object... params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public abstract T save(T entity);

    public abstract T update(T entity);
}