package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

public final class MpaRatingMapper {
    public static MpaRating mapToRating(MpaDto mpaDto) {
        return new MpaRating(mpaDto.getId(), mpaDto.getName(), mpaDto.getDescription());
    }
}
