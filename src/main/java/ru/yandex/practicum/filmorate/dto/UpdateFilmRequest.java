package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private int duration;

    @JsonProperty("mpa")
    private MpaRatingDto mpaRating;

    private Set<GenreDto> genres;
}
