package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Optional;

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
}