package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NewFilmRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    private Integer duration;

    @JsonProperty("mpa")
    private MpaRatingDto mpaRating;

    private Set<GenreDto> genres;
}
