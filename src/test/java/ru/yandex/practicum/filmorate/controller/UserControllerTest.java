package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.exception.ExceptionControllerAdvice;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {


    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Поддержка LocalDate
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("valid@example.com");
        user.setLogin("validLogin");
        user.setName("Valid User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void createUser_withValidData_shouldReturnCreatedUser() throws Exception {
        User user = createValidUser();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.name").value(user.getName()));
    }

    @Test
    void createUser_withEmptyJson_shouldReturnBadRequest() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        User user = createValidUser();
        user.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_withFutureBirthday_shouldReturnBadRequest() throws Exception {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1)); // дата рождения в будущем

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата рождения не может быть в будущем"));
    }

    @Test
    void updateUser_existingUser_shouldUpdateSuccessfully() throws Exception {
        User user = createValidUser();

        // Создаём пользователя
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);
        createdUser.setName("Updated Name");

        // Обновляем пользователя
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateUser_nonExistingUser_shouldReturnNotFound() throws Exception {
        User user = createValidUser();
        user.setId(999L);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturnAllUsers() throws Exception {
        User user1 = createValidUser();
        User user2 = createValidUser();
        user2.setEmail("second@example.com");
        user2.setLogin("secondLogin");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

}