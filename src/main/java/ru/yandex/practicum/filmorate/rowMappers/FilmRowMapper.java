package ru.yandex.practicum.filmorate.rowMappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class FilmRowMapper implements RowMapper<FilmDto> {
    @Override
    public FilmDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Создаем базовый объект FilmDto
        FilmDto filmDto = FilmDto.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date") != null ?
                        rs.getDate("release_date").toLocalDate() : null)
                .duration(rs.getLong("duration"))
                .mpa(MpaDto.builder()
                        .id(rs.getInt("mpa_id"))
                        .name(rs.getString("mpa_name"))
                        .description(rs.getString("mpa_description"))
                        .build())
                .build();

        // Добавляем режиссёров, если есть соответствующие столбцы
        if (hasColumn(rs, "director_id")) {
            Set<Director> directors = new HashSet<>();
            int currentFilmId = rs.getInt("id");

            do {
                if (!rs.wasNull() && rs.getInt("director_id") > 0) {
                    Director director = new Director();
                    director.setId(rs.getInt("director_id"));
                    director.setName(rs.getString("director_name"));
                    directors.add(director);
                }
            } while (rs.next() && rs.getInt("id") == currentFilmId);

            rs.previous(); // Возвращаем курсор на текущую строку
            filmDto.setDirectors(directors);
        }

        return filmDto;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}