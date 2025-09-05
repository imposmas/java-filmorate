package ru.yandex.practicum.filmorate.dal.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Optional;
import java.util.Set;

public interface UserStorage extends Storage<User> {
    Optional<User> findByEmail(String email);

    // Методы для работы с друзьями
    void addFriendRequest(Long userId, Long friendId, boolean status);

    void updateFriendStatus(Long userId, Long friendId, boolean status);

    void removeFriend(Long userId, Long friendId);

    Set<Long> getFriends(Long userId);

    Set<Long> getCommonFriends(Long userId1, Long userId2);

    Set<Long> getFriendRequests(Long userId);


}