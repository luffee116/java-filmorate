package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review save(Review review);

    Review update(Review review);

    void removeById(Integer id);

    Optional<Review> getReviewById(Integer id);

    List<Review> getReviewsById(Integer filmId, Integer count);

    Integer getUserIdByReviewId(Integer reviewId);

    List<Review> getAll(Integer count);

    void incrementUseful(Integer reviewId);

    void decrementUseful(Integer reviewId);

    boolean existsByUserIdAndFilmId(Integer userId, Integer filmId);

    Integer getLastReviewId();

    void updateUseful(Integer reviewId, int delta);

}