package ru.yandex.practicum.filmorate.exception;

public class SelfFriendshipException extends RuntimeException {
    public SelfFriendshipException(String message) {
        super(message);
    }
}
