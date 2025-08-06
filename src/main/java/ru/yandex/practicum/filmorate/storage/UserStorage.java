package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

public class UserStorage extends InMemoryStorage<User> {
    @Override
    protected void setId(User user, Long id) {
        user.setId(id);
    }

    @Override
    protected Long getId(User user) {
        return user.getId();
    }
}