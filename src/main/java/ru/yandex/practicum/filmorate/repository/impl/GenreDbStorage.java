package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreStorage;
import ru.yandex.practicum.filmorate.repository.TypeEntity;
import ru.yandex.practicum.filmorate.rowMappers.GenreRowMapper;

import java.util.Collection;
import java.util.Objects;

@Repository
public class GenreDbStorage extends BaseDbStorage implements GenreStorage {

    @Override
    public void setGenresForFilm(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    @Override
    public void clearGenresForFilm(int filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

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
