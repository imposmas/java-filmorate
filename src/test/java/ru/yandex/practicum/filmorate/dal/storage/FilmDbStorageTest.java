package ru.yandex.practicum.filmorate.dal.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
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
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film film1;
    private Film film2;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setLogin("testlogin");
        user.setEmail("test@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userStorage.save(user);

        film1 = new Film();
        film1.setName("Film One");
        film1.setDescription("Description One");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpaRating(new MpaRating(1L, "G")); // объект MPA
        film1.setGenres(Set.of(new Genre(1L, "Comedy"), new Genre(2L, "Drama"))); // жанры как объекты
        film1 = filmStorage.save(film1);

        film2 = new Film();
        film2.setName("Film Two");
        film2.setDescription("Description Two");
        film2.setReleaseDate(LocalDate.of(2010, 5, 5));
        film2.setDuration(150);
        film2.setMpaRating(new MpaRating(2L, "PG"));
        film2 = filmStorage.save(film2);
    }

    @Test
    void testFindAll() {
        Collection<Film> films = filmStorage.findAll();
        assertThat(films).isNotEmpty();
        assertThat(films).extracting("name").contains("Film One", "Film Two");
    }

    @Test
    void testFindById() {
        Optional<Film> filmOpt = filmStorage.findById(film1.getId());
        assertThat(filmOpt).isPresent();
        Film film = filmOpt.get();
        assertThat(film.getName()).isEqualTo("Film One");
        assertThat(film.getGenres()).extracting("id").contains(1L, 2L);
        assertThat(film.getMpaRating().getId()).isEqualTo(1L);
    }

    @Test
    void testSave() {
        Film newFilm = new Film();
        newFilm.setName("Film Three");
        newFilm.setDescription("Desc 3");
        newFilm.setReleaseDate(LocalDate.of(2020, 3, 3));
        newFilm.setDuration(90);
        newFilm.setMpaRating(new MpaRating(3L, "PG-13"));
        newFilm.setGenres(Set.of(new Genre(3L, "Animation")));

        Film savedFilm = filmStorage.save(newFilm);

        assertThat(savedFilm.getId()).isNotNull();
        Film loaded = filmStorage.findById(savedFilm.getId()).get();
        assertThat(loaded.getGenres()).extracting("id").contains(3L);
        assertThat(loaded.getMpaRating().getId()).isEqualTo(3L);
    }

    @Test
    void testUpdate() {
        film1.setName("Updated Film One");
        film1.setGenres(Set.of(new Genre(4L, "Thriller")));

        Film updatedFilm = filmStorage.update(film1);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film One");
        assertThat(filmStorage.findById(film1.getId()).get().getGenres())
                .extracting("id").contains(4L);
    }

    @Test
    void testUpdateNonExistingFilmThrows() {
        Film ghost = new Film();
        ghost.setId(999L);
        ghost.setName("Ghost Film");

        assertThrows(IllegalArgumentException.class, () -> filmStorage.update(ghost));
    }

    @Test
    void testExistsById() {
        assertThat(filmStorage.existsById(film1.getId())).isTrue();
        assertThat(filmStorage.existsById(999L)).isFalse();
    }

    @Test
    void testAddLike() {
        filmStorage.addLike(film1.getId(), user.getId());
        assertThat(filmStorage.findById(film1.getId()).get().getLikes()).contains(user.getId());
    }

    @Test
    void testRemoveLike() {
        filmStorage.addLike(film1.getId(), user.getId());
        filmStorage.removeLike(film1.getId(), user.getId());
        assertThat(filmStorage.findById(film1.getId()).get().getLikes()).doesNotContain(user.getId());
    }

    @Test
    void testGenresPersistedAndLoaded() {
        Film film = filmStorage.findById(film1.getId()).get();
        assertThat(film.getGenres()).extracting("id").contains(1L, 2L);
    }
}