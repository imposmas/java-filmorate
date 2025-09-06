package ru.yandex.practicum.filmorate.dal.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository("userDbStorage")
public class UserDbStorage extends AbstractDbStorage<User> implements UserStorage {

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "USERS", "ID", new UserRowMapper());
    }

    @Override
    public User save(User user) {
        Long id = insertAndReturnId(
                "INSERT INTO USERS (NAME, EMAIL, LOGIN, BIRTHDAY) VALUES (?, ?, ?, ?)",
                user.getName(), user.getEmail(), user.getLogin(), user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        if (!existsById(user.getId())) {
            throw new IllegalArgumentException("User not found with id " + user.getId());
        }

        jdbcTemplate.update(
                "UPDATE USERS SET NAME=?, EMAIL=?, LOGIN=?, BIRTHDAY=? WHERE ID=?",
                user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId()
        );
        return user;
    }

    @Override
    public Collection<User> findAll() {
        var users = super.findAll();
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public Optional<User> findById(Long id) {
        var userOpt = super.findById(id);
        userOpt.ifPresent(this::loadFriends);
        return userOpt;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM USERS WHERE EMAIL = ?";
        return jdbcTemplate.query(sql, new UserRowMapper(), email).stream().findFirst();
    }

    // ------------------------------------ Friends -------------------------

    private void loadFriends(User user) {
        var friendIds = jdbcTemplate.query(
                "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=?",
                (rs, rowNum) -> rs.getLong("FRIEND_ID"),
                user.getId()
        );
        user.setFriends(new HashSet<>(friendIds));
    }

    public void addFriendRequest(Long userId, Long friendId, boolean status) {
        String sql = "MERGE INTO FRIENDS (USER_ID, FRIEND_ID, STATUS) KEY(USER_ID, FRIEND_ID) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, status);
    }

    public Set<Long> getFriendRequests(Long userId) {
        String sql = "SELECT USER_ID FROM FRIENDS WHERE FRIEND_ID=? AND STATUS=?";
        var requests = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("USER_ID"),
                userId, false);
        return new HashSet<>(requests);
    }

    public void updateFriendStatus(Long userId, Long friendId, boolean status) {
        String sql = "UPDATE FRIENDS SET STATUS=? WHERE USER_ID=? AND FRIEND_ID=?";
        jdbcTemplate.update(sql, status, userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM FRIENDS WHERE USER_ID=? AND FRIEND_ID=?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public Set<Long> getFriends(Long userId) {
        String sql = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=? AND (STATUS=? OR STATUS=?)";
        var friendIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("FRIEND_ID"),
                userId, true, false);
        return new HashSet<>(friendIds);
    }

    public HashSet<Long> getCommonFriends(Long userId1, Long userId2) {
        String sql = "SELECT F1.FRIEND_ID FROM FRIENDS F1 " +
                "JOIN FRIENDS F2 ON F1.FRIEND_ID = F2.FRIEND_ID " +
                "WHERE F1.USER_ID=? AND F2.USER_ID=?";

        var commonIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("FRIEND_ID"),
                userId1,
                userId2
        );
        return new HashSet<>(commonIds);
    }
}