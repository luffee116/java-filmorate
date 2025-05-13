package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.repository.MpaRatingStorage;
import ru.yandex.practicum.filmorate.repository.TypeEntity;
import ru.yandex.practicum.filmorate.rowMappers.MpaRatingRowMapper;

import java.util.Collection;

import java.util.Objects;
import java.util.Optional;

@Repository
public class MpaRatingDbStorage extends BaseDbStorage implements MpaRatingStorage {

    private static final String GET_MPA_RATING_BY_ID_QUERY = """
            SELECT *
            FROM mpa_rating
            WHERE id = ?;
            """;
    private static final String GET_ALL_MPA_RATING_QUERY = """
            SELECT *
            FROM mpa_rating;
            """;


    public MpaRatingDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Optional<MpaRating> getMpaRating(Integer id) {
        Objects.requireNonNull(id, "Rating id can't be null");
        checkMpaRatingExist(id);
        MpaRating rating = jdbcTemplate.queryForObject(GET_MPA_RATING_BY_ID_QUERY, new MpaRatingRowMapper(), id);
        return Optional.ofNullable(rating);
    }

    @Override
    public Collection<MpaRating> getAllMpaRatings() {
        return jdbcTemplate.query(GET_ALL_MPA_RATING_QUERY, new MpaRatingRowMapper());
    }

    private void checkMpaRatingExist(Integer id) {
        checkEntityExist(id, TypeEntity.RATING);
    }
}
