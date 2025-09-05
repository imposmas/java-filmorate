package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {

    @NotNull
    private Long id;

    private String name;

    private String description;

    private LocalDate releaseDate;

    private int duration; // если 0, значит не обновляем

    private Long mpaRating;

    private Set<Long> genres;
}
