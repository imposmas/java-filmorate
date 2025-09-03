package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Optional;

public interface UserStorage extends Storage<User> {
    Optional<User> findByEmail(String email);
}