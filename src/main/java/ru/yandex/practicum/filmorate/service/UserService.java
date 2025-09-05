package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.storage.UserStorage;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.SelfFriendshipException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final UserValidator userValidator;
    private final UserMapper userMapper;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       UserValidator userValidator, UserMapper userMapper) {
        this.userStorage = userStorage;
        this.userValidator = userValidator;
        this.userMapper = userMapper;
    }

    // ---------------------------- Users ---------------------------------

    /**
     * Получение списка всех пользователей.
     *
     * @return коллекция UserDto всех пользователей
     */
    public Collection<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return UserDto пользователя
     * @throws NotFoundException если пользователь не найден
     */
    public UserDto findById(Long id) {
        return userStorage.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User with id = " + id + " not found"));
    }

    /**
     * Создание нового пользователя.
     * Производится проверка на дублирование email и валидация данных.
     *
     * @param request DTO запроса на создание пользователя
     * @return UserDto созданного пользователя
     * @throws DuplicatedDataException если email уже используется
     */
    public UserDto create(NewUserRequest request) {
        User user = userMapper.toEntity(request);
        checkUserDuplicates(user);
        userValidator.validate(user);

        User savedUser = userStorage.save(user);
        return userMapper.toDto(savedUser);
    }

    /**
     * Обновление существующего пользователя.
     *
     * @param request DTO запроса на обновление пользователя
     * @return UserDto обновленного пользователя
     * @throws NotFoundException если пользователь с указанным ID не найден
     */
    public UserDto update(UpdateUserRequest request) {
        User existingUser = userStorage.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        userMapper.updateEntity(existingUser, request);
        checkUserDuplicates(existingUser);
        userValidator.validate(existingUser);

        User updatedUser = userStorage.update(existingUser);
        return userMapper.toDto(updatedUser);
    }

    // ---------------------------- Friends ---------------------------------

    /**
     * Добавление друга для пользователя.
     * Если существует обратная заявка от friendId к userId,
     * статус дружбы обновляется на "подтверждено".
     * Если обратной заявки нет — создается исходящая заявка.
     *
     * @param userId   ID пользователя
     * @param friendId ID друга
     * @throws SelfFriendshipException если userId == friendId
     * @throws NotFoundException       если один из пользователей не найден
     */
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new SelfFriendshipException("Not possible to add yourself to friends");
        }

        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("Friend not found"));

        Set<Long> reverseRequests = userStorage.getFriendRequests(friendId);
        if (reverseRequests.contains(userId)) {
            // подтверждаем взаимную дружбу
            userStorage.updateFriendStatus(friendId, userId, true);
            userStorage.updateFriendStatus(userId, friendId, true);
        } else {
            // создаем исходящую заявку
            userStorage.addFriendRequest(userId, friendId, false);
        }
    }

    /**
     * Удаление друга для пользователя.
     * После вызова метод удаляет запись дружбы.
     *
     * @param userId   ID пользователя
     * @param friendId ID друга
     * @throws NotFoundException если один из пользователей не найден
     */
    public void removeFriend(Long userId, Long friendId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("User not found"));

        userStorage.removeFriend(userId, friendId);
    }

    /**
     * Получение списка друзей пользователя.
     *
     * @param userId ID пользователя
     * @return коллекция UserDto друзей пользователя
     * @throws NotFoundException если пользователь не найден
     */
    public Collection<UserDto> getFriends(Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        return userStorage.getFriends(userId).stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    /**
     * Получение списка общих друзей между двумя пользователями.
     *
     * @param userId  ID первого пользователя
     * @param otherId ID второго пользователя
     * @return коллекция UserDto общих друзей
     */
    public Collection<UserDto> getCommonFriends(Long userId, Long otherId) {
        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    /**
     * Проверка на дублирование email при создании/обновлении пользователя.
     *
     * @param user пользователь для проверки
     * @throws DuplicatedDataException если email уже используется другим пользователем
     */
    private void checkUserDuplicates(User user) {
        boolean duplicate = userStorage.findByEmail(user.getEmail())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .isPresent();
        if (duplicate) {
            throw new DuplicatedDataException("Email is already in use");
        }
    }
}