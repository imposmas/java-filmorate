package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private Long id;

    private String name;

    @Email
    private String email;

    @Pattern(regexp = "^\\S+$")
    private String login;

    private LocalDate birthday;
}
