package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@ToString
@EqualsAndHashCode(of = {"email"})
public class User {
    Long id;
    String name;
    @NotBlank
    @Email
    String email;
    @NotBlank
    @Pattern(regexp = "^\\S+$")
    String login;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate birthday;
    Set<Long> friends = new HashSet<>();
}