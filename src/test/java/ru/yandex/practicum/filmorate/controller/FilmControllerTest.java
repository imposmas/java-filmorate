package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.exception.ExceptionControllerAdvice;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorageImpl;
import ru.yandex.practicum.filmorate.storage.UserStorageImpl;
import ru.yandex.practicum.filmorate.validator.FilmValidator;
import ru.yandex.practicum.filmorate.validator.UserValidator;

import java.time.LocalDate;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilmControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserService userService;
    private UserController userController;
    private FilmService filmService;
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        userService = new UserService(new UserStorageImpl(), new UserValidator());
        filmService = new FilmService(new FilmStorageImpl(), new FilmValidator(), userService);
        userController = new UserController(userService);
        filmController = new FilmController(filmService);

        mockMvc = MockMvcBuilders.standaloneSetup(userController, filmController)
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("valid@example.com");
        user.setLogin("validLogin");
        user.setName("Valid User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Film createValidFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Some description for " + name);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setLikes(new HashSet<>());
        return film;
    }

    @Test
    void createFilm_withValidData_shouldReturnCreatedFilm() throws Exception {
        Film film = createValidFilm("Matrix");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()));
    }

    @Test
    void createFilm_withDuplicate_shouldReturnConflict() throws Exception {
        Film film = createValidFilm("Matrix 2");

        // создаём первый раз — успешно
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());

        // создаём второй раз — дубликат, ожидаем 409 Conflict
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isConflict());
    }

    @Test
    void createFilm_withTooLongDescription_shouldReturnBadRequest() throws Exception {
        Film film = createValidFilm("Matrix 3");
        film.setDescription("A".repeat(201)); // описание длиной 201 символ

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Описание фильма не может превышать 200 символов"));
    }

    @Test
    void createFilm_withReleaseDateBefore18951228_shouldReturnBadRequest() throws Exception {
        Film film = createValidFilm("Matrix 4");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // дата до 28.12.1895

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    @Test
    void createFilm_withNegativeDuration_shouldReturnBadRequest() throws Exception {
        Film film = createValidFilm("Matrix 5");
        film.setDuration(-10); // отрицательная длительность

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void updateFilm_existingFilm_shouldUpdateSuccessfully() throws Exception {
        Film film = createValidFilm("Matrix 6");

        // создаём фильм
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);
        createdFilm.setName("The Matrix Reloaded");

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("The Matrix Reloaded"));
    }

    @Test
    void updateFilm_nonExistingFilm_shouldReturnNotFound() throws Exception {
        Film film = createValidFilm("Matrix 7");
        film.setId(999L); // несуществующий ID

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturnAllFilms() throws Exception {
        Film film1 = createValidFilm("Matrix 7");

        Film film2 = new Film();
        film2.setName("Inception");
        film2.setDescription("A thief who steals corporate secrets through dream-sharing technology.");
        film2.setReleaseDate(LocalDate.of(2010, 7, 16));
        film2.setDuration(148);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void addLikeToFilm_shouldIncreaseLikeCount() throws Exception {
        User user = createValidUser();
        Film film = createValidFilm("Matrix 8");

        String userStr = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String filmStr = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(userStr, User.class);
        Film createdFilm = objectMapper.readValue(filmStr, Film.class);

        // Добавляем лайк
        mockMvc.perform(put("/films/{id}/like/{userId}", createdFilm.getId(), createdUser.getId()))
                .andExpect(status().isOk());

        // Проверяем, что лайк добавлен
        mockMvc.perform(get("/films/{id}", createdFilm.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes.length()").value(1));
    }

    @Test
    void removeLikeFromFilm_shouldDecreaseLikeCount() throws Exception {
        User user = createValidUser();
        Film film = createValidFilm("Matrix 9");

        String userStr = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andReturn().getResponse().getContentAsString();

        String filmStr = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(userStr, User.class);
        Film createdFilm = objectMapper.readValue(filmStr, Film.class);

        mockMvc.perform(put("/films/{id}/like/{userId}", createdFilm.getId(), createdUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/films/{id}/like/{userId}", createdFilm.getId(), createdUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/{id}", createdFilm.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes.length()").value(0));
    }

    @Test
    void getMostPopularFilms_shouldReturnSortedByLikes() throws Exception {
        User user1 = createValidUser();
        User user2 = createValidUser();
        user2.setEmail("second@example.com");
        user2.setLogin("secondLogin");

        Film film1 = createValidFilm("Matrix 10");
        Film film2 = createValidFilm("Matrix 11");

        // Создаем пользователей
        String userStr1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andReturn().getResponse().getContentAsString();
        String userStr2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andReturn().getResponse().getContentAsString();

        User u1 = objectMapper.readValue(userStr1, User.class);
        User u2 = objectMapper.readValue(userStr2, User.class);

        // Создаем фильмы
        String filmStr1 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andReturn().getResponse().getContentAsString();
        String filmStr2 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andReturn().getResponse().getContentAsString();

        Film f1 = objectMapper.readValue(filmStr1, Film.class);
        Film f2 = objectMapper.readValue(filmStr2, Film.class);

        // Лайки: film1 — 2 лайка, film2 — 1 лайк
        mockMvc.perform(put("/films/{id}/like/{userId}", f1.getId(), u1.getId())).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", f1.getId(), u2.getId())).andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", f2.getId(), u1.getId())).andExpect(status().isOk());

        // Проверяем топ популярных фильмов
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(f1.getId()))
                .andExpect(jsonPath("$[1].id").value(f2.getId()));
    }
}