package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

public final class ReviewMapper {
    public static Review mapToReview(ReviewDto reviewDto) {
        return new Review().toBuilder()
                .reviewId(reviewDto.getReviewId())
                .content(reviewDto.getContent())
                .filmId(reviewDto.getFilmId())
                .userId(reviewDto.getUserId())
                .isPositive(reviewDto.getIsPositive())
                .useful(reviewDto.getUseful() == null ? 0 : reviewDto.getUseful())
                .build();
    }
}
