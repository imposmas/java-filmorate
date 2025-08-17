package ru.yandex.practicum.filmorate.validator;

public interface Validator<T> {
    void validate(T input);
}
