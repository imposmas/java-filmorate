package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("ID"));
        film.setName(resultSet.getString("NAME"));
        film.setDescription(resultSet.getString("DESCRIPTION"));
        film.setDuration(resultSet.getInt("DURATION"));

        Date releaseDate = resultSet.getDate("RELEASE_DATE");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        long mpaRating = resultSet.getLong("MPA_RATING");
        film.setMpaRating(resultSet.wasNull() ? null : mpaRating);

        return film;
    }
}
