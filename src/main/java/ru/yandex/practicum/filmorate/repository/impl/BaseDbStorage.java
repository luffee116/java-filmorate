package ru.yandex.practicum.filmorate.repository.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.repository.TypeEntity;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class BaseDbStorage {
    protected final JdbcTemplate jdbcTemplate;

    private static final String CHECK_USER_EXIST_BY_ID_QUERY = "SELECT COUNT(*) FROM users WHERE id = ?; ";
    private static final String CHECK_FILM_EXIST_BY_ID_QUERY = "SELECT COUNT(*) FROM films WHERE id = ?; ";
    private static final String CHECK_GENRE_EXIST_BY_ID_QUERY = "SELECT COUNT(*) FROM genres WHERE genre_id = ?; ";
    private static final String CHECK_RATING_EXIST_BY_ID_QUERY = "SELECT COUNT(*) FROM mpa_rating WHERE id = ?; ";
    private static final String CHECK_REVIEW_EXIST_BY_ID_QUERY = "SELECT COUNT(*) FROM reviews WHERE review_id = ?; ";
    protected static final String CHECK_EXISTS_DIRECTOR_BY_ID_QUERY = """
            SELECT COUNT(*)
            FROM directors
            WHERE id = ?;
            """;

    protected void checkEntityExist(Integer id, TypeEntity typeEntity) {
        String sql = getQuery(typeEntity);

        int count = Optional.ofNullable(jdbcTemplate.queryForObject(sql, Integer.class, id)).orElse(0);

        if (count == 0) {
            String errorMessage = String.format("Объект %s с id %s не найден ", typeEntity, id);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private String getQuery(TypeEntity typeEntity) {
        return switch (typeEntity) {
            case GENRE -> CHECK_GENRE_EXIST_BY_ID_QUERY;
            case FILM -> CHECK_FILM_EXIST_BY_ID_QUERY;
            case RATING -> CHECK_RATING_EXIST_BY_ID_QUERY;
            case USER -> CHECK_USER_EXIST_BY_ID_QUERY;
            case REVIEW -> CHECK_REVIEW_EXIST_BY_ID_QUERY;
            case DIRECTOR -> CHECK_EXISTS_DIRECTOR_BY_ID_QUERY;
        };
    }
    protected void checkEntityExists(Integer id, TypeEntity type) {
        String query = getQuery(type);

        int count = Optional.ofNullable(jdbcTemplate.queryForObject(query, Integer.class, id)).orElse(0);

        if (count == 0) {
            String errorMessage = String.format("%s with id=%d not found", type, id);
            log.warn(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

}