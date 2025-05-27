package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseDbStorage implements DirectorStorage {

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate); // передаем в родительский класс
    }

    @Override
    public void setDirectorsForFilm(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";

        for (Director director : film.getDirectors()) {
            jdbcTemplate.update(sql, film.getId(), director.getId());
        }
    }

    @Override
    public void clearDirectorsForFilm(int filmId) {
        String sql = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors(name) VALUES(?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        jdbcTemplate.update("UPDATE directors SET name = ? WHERE id = ?", director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM directors WHERE id = ?", id);
    }

    @Override
    public Optional<Director> getById(int id) {
        String sql = "SELECT id, name FROM directors WHERE id = ?";
        List<Director> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(rs.getInt("id"), rs.getString("name")), id);
        return list.stream().findFirst();
    }

    @Override
    public List<Director> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM directors", (rs, rowNum) ->
                new Director(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}