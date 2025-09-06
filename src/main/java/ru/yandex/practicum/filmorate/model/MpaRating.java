package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
public class MpaRating {
    private Long id;
    private String name;

    public MpaRating() {
    }

    public MpaRating(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
