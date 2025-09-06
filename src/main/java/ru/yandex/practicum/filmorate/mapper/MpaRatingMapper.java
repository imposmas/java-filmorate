package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

@Component
public class MpaRatingMapper {

    public MpaRatingDto toDto(MpaRating mpa) {
        if (mpa == null) return null;

        MpaRatingDto dto = new MpaRatingDto();
        dto.setId(mpa.getId());
        dto.setName(mpa.getName());
        return dto;
    }
}
