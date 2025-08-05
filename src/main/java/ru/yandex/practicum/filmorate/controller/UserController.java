package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {

        log.debug("createUser parameters: name {}, email {}, login {}, birthday {}:  ", user.getName(),
                user.getEmail(), user.getLogin(), user.getBirthday());
        checkUserDuplicates(user);
        UserValidator userParametersValidator = new UserValidator();
        userParametersValidator.validate(user);
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.debug("updateUser parameters: id {}, name {}, email {}, login {}, birthday {}:  ", newUser.getId(),
                newUser.getName(), newUser.getEmail(), newUser.getLogin(), newUser.getBirthday());
        if (isUserExistsById(newUser)) {
            checkUserDuplicates(newUser);
            users.put(newUser.getId(), newUser);
            return newUser;
        }
        throw new NotFoundException("Юзер с id = " + newUser.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkUserDuplicates(User user) {
        if (users.values().stream()
                .anyMatch(existingUser -> !existingUser.getId().equals(user.getId())
                        && existingUser.equals(user))) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
    }

    private boolean isUserExistsById(User user) {
        return users.containsKey(user.getId());
    }
}