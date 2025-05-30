package ru.yandex.practicum.filmorate.repository;

import java.util.Optional;

public interface ReviewRatingStorage {

    void addRating(Integer reviewId, Integer userId, boolean isLike);

    void updateRating(Integer reviewId, Integer userId, boolean isLike);

    void deleteRating(Integer reviewId, Integer userId);

    Optional<Boolean> getRating(Integer reviewId, Integer userId);

}
