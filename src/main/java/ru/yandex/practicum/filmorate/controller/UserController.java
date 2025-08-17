package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorage userStorage = new UserStorage();

    @GetMapping
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.debug("createUser parameters: name {}, email {}, login {}, birthday {}",
                user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());

        checkUserDuplicates(user);
        new UserValidator().validate(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userStorage.save(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.debug("updateUser parameters: id {}, name {}, email {}, login {}, birthday {}",
                newUser.getId(), newUser.getName(), newUser.getEmail(),
                newUser.getLogin(), newUser.getBirthday());

        if (!userStorage.existsById(newUser.getId())) {
            throw new NotFoundException("Юзер с id = " + newUser.getId() + " не найден");
        }

        checkUserDuplicates(newUser);
        return userStorage.update(newUser);
    }

    private void checkUserDuplicates(User user) {
        boolean duplicate = userStorage.findAll().stream()
                .anyMatch(existingUser ->
                        !existingUser.getId().equals(user.getId()) &&
                                existingUser.equals(user)
                );

        if (duplicate) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
    }
}