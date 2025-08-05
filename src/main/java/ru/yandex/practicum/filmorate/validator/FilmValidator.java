package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmValidator implements Validator<Film> {
    public FilmValidator() {
    }

    public void validate(Film film) {
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может превышать 200 символов");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        } else if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
