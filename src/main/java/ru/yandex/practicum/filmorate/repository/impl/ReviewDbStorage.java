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
        review.setId(reviewId);
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
        checkEntityExist(review.getId(), TypeEntity.REVIEW);

        jdbcTemplate.update(
                UPDATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
                review.getId()
        );

        log.info("Review updated: {}", review);
        return review;
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
     * @param film_id идентификатор фильма, по которому необходимо получить отзывы
     * @param count   количество выводимых отзывов (Необязателен, в таком случае default = 10)
     */
    @Override
    public List<Review> getReviewsById(Integer film_id, Integer count) {
        if (count == null) {
            count = 10; // Установка базового значения
        }
        log.info("Get reviews by id: {}", film_id);
        return jdbcTemplate.query(GET_REVIEWS_BY_FILM_ID_QUERY, new ReviewRowMapper(), film_id, count);
    }

    /**
     * Получение списка всех отзывов БД
     *
     * @param count количество выводимых отзывов
     */
    @Override
    public List<Review> getAll(Integer count) {
        if (count == null) {
            count = 10; // установка базового значения
        }
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


}
