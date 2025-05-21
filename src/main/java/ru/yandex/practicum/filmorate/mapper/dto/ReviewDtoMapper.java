package ru.yandex.practicum.filmorate.mapper.dto;

import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

public final class ReviewDtoMapper {
    public static ReviewDto mapToDto(final Review review) {
        return new ReviewDto().toBuilder()
                .id(review.getId())
                .content(review.getContent())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .isPositive(review.getIsPositive())
                .useful(review.getUseful())
                .build();
    }
}
