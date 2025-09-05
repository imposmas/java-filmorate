package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.storage.UserStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Optional;
import java.util.Set;

@Deprecated
@Repository
public class UserStorageImpl extends InMemoryStorage<User> implements UserStorage {
    @Override
    protected void setId(User user, Long id) {
        user.setId(id);
    }

    @Override
    protected Long getId(User user) {
        return user.getId();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public void addFriendRequest(Long userId, Long friendId, boolean status) {

    }

    @Override
    public void updateFriendStatus(Long userId, Long friendId, boolean status) {

    }

    @Override
    public void removeFriend(Long userId, Long friendId) {

    }

    @Override
    public Set<Long> getFriends(Long userId) {
        return Set.of();
    }

    @Override
    public Set<Long> getCommonFriends(Long userId1, Long userId2) {
        return Set.of();
    }

    @Override
    public Set<Long> getFriendRequests(Long userId) {
        return Set.of();
    }
}