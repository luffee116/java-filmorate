package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;
import ru.yandex.practicum.filmorate.repository.TypeEntity;
import ru.yandex.practicum.filmorate.rowMappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewDbStorage extends BaseDbStorage implements ReviewStorage {
    private static final String CREATE_REVIEW_QUERY = """
            INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, ?);
            """;
    private static final String UPDATE_REVIEW_QUERY = """
            UPDATE reviews
            SET content = ?, is_positive = ?, useful = ?
            WHERE review_id = ?;
            """;
    private static final String DELETE_REVIEW_QUERY = """
            DELETE FROM reviews
            WHERE review_id = ?;
            """;
    private static final String GET_REVIEW_BY_ID_QUERY = """
            SELECT * FROM reviews WHERE review_id = ?;
            """;
    private static final String GET_REVIEWS_BY_FILM_ID_QUERY = """
            SELECT * FROM reviews
            WHERE film_id = ?
            ORDER BY useful DESC
            LIMIT ?;
            """;
    private static final String GET_ALL_REVIEWS_QUERY = """
            SELECT * FROM reviews
            ORDER BY useful DESC
            LIMIT ?;
            """;
    private static final String INCREMENT_USEFUL_QUERY = """
            UPDATE reviews SET useful = useful + 1 WHERE review_id = ?;
            """;
    private static final String DECREMENT_USEFUL_QUERY = """
            UPDATE reviews SET useful = useful - 1 WHERE review_id = ?;
            """;
    private static final String GET_REVIEW_BY_FILM_AND_USER_ID = """
            SELECT * FROM reviews WHERE film_id = ? AND user_id = ?;
            """;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    /**
     * Сохранение отзыва в БД
     *
     * @param review принимаемый для сохранения отзыв
     */
    @Override
    public Review save(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_REVIEW_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

        Integer reviewId = keyHolder.getKey().intValue();
        review.setReviewId(reviewId);
        log.info("Review created: {}", reviewId);
        return review;
    }

    /**
     * Обновление отзыва в БД
     *
     * @param review принимаемый для обновления отзыв
     */
    @Override
    public Review update(Review review) {
        if (review.getReviewId() != null) {
            checkEntityExist(review.getReviewId(), TypeEntity.REVIEW);
        }

        Review reviewToUpdate = jdbcTemplate.queryForObject(
                GET_REVIEW_BY_FILM_AND_USER_ID,
                new ReviewRowMapper(),
                review.getFilmId(),
                review.getUserId());

        jdbcTemplate.update(
                UPDATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
                reviewToUpdate.getReviewId()
        );

        Review reviewToResponse = jdbcTemplate.queryForObject(
                GET_REVIEW_BY_ID_QUERY,
                new ReviewRowMapper(),
                reviewToUpdate.getReviewId());

        log.info("Review updated: {}", review);
        return reviewToResponse;
    }

    /**
     * Удаление отзыва в БД
     *
     * @param reviewId идентификатор удаляемого отзыва
     */
    @Override
    public void removeById(Integer reviewId) {
        checkEntityExist(reviewId, TypeEntity.REVIEW);
        jdbcTemplate.update(DELETE_REVIEW_QUERY, reviewId);
        log.info("Review removed: {}", reviewId);
    }

    /**
     * Получение отзыва из БД по идентификатору
     *
     * @param reviewId идентификатор отзыва
     */
    @Override
    public Optional<Review> getReviewById(Integer reviewId) {
        checkEntityExist(reviewId, TypeEntity.REVIEW);
        Review review = jdbcTemplate.queryForObject(GET_REVIEW_BY_ID_QUERY, new ReviewRowMapper(), reviewId);

        if (review == null) {
            return Optional.empty();
        }
        return Optional.of(review);
    }

    /**
     * Получения списка отзывов из БД
     *
     * @param filmId идентификатор фильма, по которому необходимо получить отзывы
     * @param count  количество выводимых отзывов (Необязателен, в таком случае default = 10)
     */
    @Override
    public List<Review> getReviewsById(Integer filmId, Integer count) {
        log.info("Get reviews by id: {}", filmId);
        return jdbcTemplate.query(GET_REVIEWS_BY_FILM_ID_QUERY, new ReviewRowMapper(), filmId, count);
    }

    /**
     * Получение списка всех отзывов БД
     *
     * @param count количество выводимых отзывов
     */
    @Override
    public List<Review> getAll(Integer count) {
        log.info("Get reviews all: {}", count);
        return jdbcTemplate.query(GET_ALL_REVIEWS_QUERY, new ReviewRowMapper(), count);
    }

    /**
     * Повышение полезности отзыва
     *
     * @param reviewId идентификатор отзыва
     */
    @Override
    public void incrementUseful(Integer reviewId) {
        jdbcTemplate.update(INCREMENT_USEFUL_QUERY, reviewId);
    }

    /**
     * Понижение полезности отзыва
     *
     * @param reviewId идентификатор отзыва
     */
    @Override
    public void decrementUseful(Integer reviewId) {
        jdbcTemplate.update(DECREMENT_USEFUL_QUERY, reviewId);
    }

    @Override
    public boolean existsByUserIdAndFilmId(Integer userId, Integer filmId) {
        String sql = "SELECT COUNT(*) > 0 FROM reviews WHERE user_id = ? AND film_id = ?";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, filmId));
    }

    @Override
    public Integer getLastReviewId() {
        String sql = """
                SELECT MAX(REVIEW_ID)
                FROM reviews
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }


}
