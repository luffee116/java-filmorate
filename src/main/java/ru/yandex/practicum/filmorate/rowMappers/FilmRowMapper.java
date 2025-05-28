package ru.yandex.practicum.filmorate.rowMappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilmRowMapper implements RowMapper<FilmDto> {
    Set<GenreDto> genres = new HashSet<>();
    Set<Integer> likes = new HashSet<>();
    Map<Integer, String> reviews = new HashMap<>();

    @Override
    public FilmDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FilmDto.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getLong("duration"))
                .mpa(MpaDto.builder()
                        .id(rs.getInt("mpa_id"))
                        .name(rs.getString("mpa_name"))
                        .description(rs.getString("mpa_description"))
                        .build())
                .genres(genres)
                .likes(likes)
                .review(reviews)
                .build();
    }
}
