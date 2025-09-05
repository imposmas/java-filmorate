package ru.yandex.practicum.filmorate.dal.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("User One");
        user1.setLogin("user1");
        user1.setEmail("user1@test.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userStorage.save(user1);

        user2 = new User();
        user2.setName("User Two");
        user2.setLogin("user2");
        user2.setEmail("user2@test.com");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        user2 = userStorage.save(user2);
    }

    @Test
    void testFindAll() {
        Collection<User> users = userStorage.findAll();
        assertThat(users).isNotEmpty();
        assertThat(users).extracting("email").contains("user1@test.com", "user2@test.com");
    }

    @Test
    void testFindById() {
        Optional<User> userOptional = userStorage.findById(user1.getId());
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getName()).isEqualTo("User One");
    }

    @Test
    void testSave() {
        User newUser = new User();
        newUser.setName("User Three");
        newUser.setLogin("user3");
        newUser.setEmail("user3@test.com");
        newUser.setBirthday(LocalDate.of(2000, 3, 3));

        User savedUser = userStorage.save(newUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(userStorage.findById(savedUser.getId())).isPresent();
    }

    @Test
    void testUpdate() {
        user1.setName("Updated Name");
        User updatedUser = userStorage.update(user1);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(userStorage.findById(user1.getId()).get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void testUpdateNonExistingUserThrows() {
        User nonExisting = new User();
        nonExisting.setId(999L);
        nonExisting.setName("Ghost");

        assertThrows(IllegalArgumentException.class, () -> userStorage.update(nonExisting));
    }

    @Test
    void testExistsById() {
        assertThat(userStorage.existsById(user1.getId())).isTrue();
        assertThat(userStorage.existsById(999L)).isFalse();
    }

    @Test
    void testFindByEmail() {
        Optional<User> userOptional = userStorage.findByEmail("user1@test.com");
        assertThat(userOptional).isPresent();
        assertThat(userOptional.get().getLogin()).isEqualTo("user1");
    }

    @Test
    void testAddFriendRequestAndGetRequests() {
        userStorage.addFriendRequest(user1.getId(), user2.getId(), false);

        Set<Long> requests = userStorage.getFriendRequests(user2.getId());
        assertThat(requests).contains(user1.getId());
    }

    @Test
    void testUpdateFriendStatus() {
        userStorage.addFriendRequest(user1.getId(), user2.getId(), false);
        userStorage.updateFriendStatus(user1.getId(), user2.getId(), true);

        Set<Long> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).contains(user2.getId());
    }

    @Test
    void testRemoveFriend() {
        userStorage.addFriendRequest(user1.getId(), user2.getId(), true);
        userStorage.removeFriend(user1.getId(), user2.getId());

        Set<Long> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).doesNotContain(user2.getId());
    }

    @Test
    void testGetFriends() {
        userStorage.addFriendRequest(user1.getId(), user2.getId(), true);

        Set<Long> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).contains(user2.getId());
    }

    @Test
    void testGetCommonFriends() {
        User user3 = new User();
        user3.setName("User Three");
        user3.setLogin("user3");
        user3.setEmail("user3@test.com");
        user3.setBirthday(LocalDate.of(2000, 3, 3));
        user3 = userStorage.save(user3);

        userStorage.addFriendRequest(user1.getId(), user3.getId(), true);
        userStorage.addFriendRequest(user2.getId(), user3.getId(), true);

        Set<Long> common = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(common).contains(user3.getId());
    }
}