package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.repository.ReviewRatingStorage;

import java.util.Optional;

@Repository
@Slf4j
public class ReviewRatingDbStorage extends BaseDbStorage implements ReviewRatingStorage {
    private static final String ADD_RATING_QUERY = """
            INSERT INTO review_ratings (review_id, user_id, is_like)
            VALUES (?, ?, ?);
            """;
    private static final String UPDATE_RATING_QUERY = """
            UPDATE review_ratings
            SET is_like = ?
            WHERE review_id = ? AND user_id = ?;
            """;
    private static final String DELETE_RATING_QUERY = """
            DELETE FROM review_ratings
            WHERE review_id = ? AND user_id = ?;
            """;
    private static final String GET_RATING_QUERY = """
            SELECT IS_LIKE FROM review_ratings
            WHERE review_id = ? AND user_id = ?;
            """;

    public ReviewRatingDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    /**
     * Добавление рейтинга отзыва
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     * @param isLike   тип отзыва (положительный - true / отрицательный - false)
     */
    @Override
    public void addRating(Integer reviewId, Integer userId, boolean isLike) {
        jdbcTemplate.update(ADD_RATING_QUERY, reviewId, userId, isLike);
        log.info("Rating added: review {}, user {}, isLike {}", reviewId, userId, isLike);
    }

    /**
     * Обновление рейтинга отзыва
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     * @param isLike   тип отзыва (положительный - true / отрицательный - false)
     */
    @Override
    public void updateRating(Integer reviewId, Integer userId, boolean isLike) {
        jdbcTemplate.update(UPDATE_RATING_QUERY, reviewId, userId, isLike);
        log.info("Rating updated: review {}, user {}, isLike {}", reviewId, userId, isLike);
    }

    /**
     * Удаление рейтинга отзыва
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    @Override
    public void deleteRating(Integer reviewId, Integer userId) {
        jdbcTemplate.update(DELETE_RATING_QUERY, reviewId, userId);
        log.info("Rating deleted: review {}, user {}", reviewId, userId);
    }

    /**
     * Получение типа отзыва (положительный/негативный)
     *
     * @param reviewId идентификатор отзыва
     * @param userId   идентификатор пользователя
     */
    @Override
    public Optional<Boolean> getRating(Integer reviewId, Integer userId) {
        return jdbcTemplate.query(GET_RATING_QUERY,
                rs -> rs.next() ? Optional.of(rs.getBoolean("IS_LIKE")) : Optional.empty(),
                reviewId, userId
        );
    }
}
