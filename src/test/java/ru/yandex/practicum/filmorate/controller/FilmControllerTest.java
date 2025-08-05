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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilmControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        FilmController controller = new FilmController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionControllerAdvice())  // обработка исключений
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // поддержка LocalDate
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("The Matrix");
        film.setDescription("A computer hacker learns about the true nature of reality.");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        return film;
    }

    @Test
    void createFilm_withValidData_shouldReturnCreatedFilm() throws Exception {
        Film film = createValidFilm();

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
        Film film = createValidFilm();

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
        Film film = createValidFilm();
        film.setDescription("A".repeat(201)); // описание длиной 201 символ

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").
                        value("Описание фильма не может превышать 200 символов"));
    }

    @Test
    void createFilm_withReleaseDateBefore18951228_shouldReturnBadRequest() throws Exception {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // дата до 28.12.1895

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").
                        value("Дата релиза — не раньше 28 декабря 1895 года"));
    }

    @Test
    void createFilm_withNegativeDuration_shouldReturnBadRequest() throws Exception {
        Film film = createValidFilm();
        film.setDuration(-10); // отрицательная длительность

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").
                        value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void updateFilm_existingFilm_shouldUpdateSuccessfully() throws Exception {
        Film film = createValidFilm();

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

        // обновляем фильм
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("The Matrix Reloaded"));
    }

    @Test
    void updateFilm_nonExistingFilm_shouldReturnNotFound() throws Exception {
        Film film = createValidFilm();
        film.setId(999L); // несуществующий ID

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturnAllFilms() throws Exception {
        Film film1 = createValidFilm();

        Film film2 = new Film();
        film2.setName("Inception");
        film2.setDescription("A thief who steals corporate secrets through dream-sharing technology.");
        film2.setReleaseDate(LocalDate.of(2010, 7, 16));
        film2.setDuration(148);

        // добавляем фильмы
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isOk());

        // проверяем, что их два
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

}