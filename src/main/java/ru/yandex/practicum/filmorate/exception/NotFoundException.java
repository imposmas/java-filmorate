package ru.yandex.practicum.filmorate.exception;

public class NotFoundException extends NullPointerException {
    public NotFoundException(String message) {
        super(message);
    }
}
