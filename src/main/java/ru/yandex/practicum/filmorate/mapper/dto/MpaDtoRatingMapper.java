package ru.yandex.practicum.filmorate.mapper.dto;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

public final class MpaDtoRatingMapper {
    public static MpaDto mapToDto(MpaRating rating) {
        return MpaDto.builder()
                .id(rating.getId())
                .name(rating.getName())
                .description(rating.getDescription())
                .build();
    }
}
