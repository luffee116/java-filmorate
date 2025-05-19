package ru.yandex.practicum.filmorate.repository.impl;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreStorage;
import ru.yandex.practicum.filmorate.repository.TypeEntity;
import ru.yandex.practicum.filmorate.rowMappers.GenreRowMapper;

import java.util.Collection;
import java.util.Objects;

@Repository
public class GenreDbStorage extends BaseDbStorage implements GenreStorage {

    private static final String GET_GENRE_QUERY = """
            SELECT * FROM genres WHERE genre_id = ?;
            """;

    private static final String GET_ALL_GENRE_QUERY = """
            SELECT *
            FROM genres
            ORDER BY genre_id ASC;
            """;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Genre getGenre(Integer id) {
        Objects.requireNonNull(id, "Genre id can't be null");
        checkGenreExist(id);
        return jdbcTemplate.queryForObject(GET_GENRE_QUERY, new GenreRowMapper(), id);
    }

    @Override
    public Collection<Genre> getAllGenres() {
        return jdbcTemplate.query(GET_ALL_GENRE_QUERY, new GenreRowMapper());
    }

    private void checkGenreExist(Integer id) {
        checkEntityExist(id, TypeEntity.GENRE);
    }
}
