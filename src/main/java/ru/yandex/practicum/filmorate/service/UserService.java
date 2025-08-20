package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorageImpl;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorageImpl userStorageImpl;
    private final UserValidator userValidator;

    public UserService(UserStorageImpl userStorageImpl, UserValidator userValidator) {
        this.userStorageImpl = userStorageImpl;
        this.userValidator = userValidator;
    }

    public Collection<User> findAll() {
        return userStorageImpl.findAll();
    }

    public User findById(Long id) {
        return userStorageImpl.findById(id)
                .orElseThrow(() -> new NotFoundException("Юзер с id = " + id + " не найден"));
    }

    public User create(User user) {
        log.debug("createUser parameters: name {}, email {}, login {}, birthday {}",
                user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());

        checkUserDuplicates(user);
        userValidator.validate(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userStorageImpl.save(user);
    }

    public User update(User newUser) {
        log.debug("updateUser parameters: id {}, name {}, email {}, login {}, birthday {}",
                newUser.getId(), newUser.getName(), newUser.getEmail(),
                newUser.getLogin(), newUser.getBirthday());

        userStorageImpl.findById(newUser.getId())
                .orElseThrow(() -> new NotFoundException("Юзер с id = " + newUser.getId() + " не найден"));

        checkUserDuplicates(newUser);
        return userStorageImpl.update(newUser);
    }

    public void addFriend(Long userId, Long friendId) {
        log.debug("addFriend parameters: userId {}, film {}", userId, friendId);
        findById(userId).getFriends().add(friendId);
        findById(friendId).getFriends().add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.debug("removeFriend parameters: userId {}, film {}", userId, friendId);
        findById(userId).getFriends().remove(friendId);
        findById(friendId).getFriends().remove(userId);
    }

    public Collection<User> getFriends(Long userId) {
        return findById(userId).getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> commonIds = findById(userId).getFriends().stream()
                .filter(findById(otherId).getFriends()::contains)
                .collect(Collectors.toSet());

        return commonIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    private void checkUserDuplicates(User user) {
        boolean duplicate = userStorageImpl.findByEmail(user.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .isPresent();

        if (duplicate) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
    }
}
