package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewUserRequest {
    @NotBlank
    private String login;

    @Email
    @NotBlank
    private String email;

    private String name;

    private LocalDate birthday;
}
