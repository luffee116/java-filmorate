package ru.yandex.practicum.filmorate.mapper.dto;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Component
public final class FilmDtoMapper {

    public FilmDto mapToFilmDto(ResultSet rs) throws SQLException {
        return FilmDto.builder()
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
    }

    public static FilmDto mapToFilmDto(Film film) {
        if (film.getGenres() == null) {
            return FilmDto.builder()
                    .id(film.getId())
                    .name(film.getName())
                    .description(film.getDescription())
                    .releaseDate(film.getReleaseDate())
                    .duration(film.getDuration())
                    .mpa(MpaDtoRatingMapper.mapToDto(film.getMpa()))
                    .likes(film.getLikes())
                    .review(film.getReview())
                    .build();
        } else {
            return FilmDto.builder()
                    .id(film.getId())
                    .name(film.getName())
                    .description(film.getDescription())
                    .releaseDate(film.getReleaseDate())
                    .duration(film.getDuration())
                    .mpa(MpaDtoRatingMapper.mapToDto(film.getMpa()))
                    .genres(film.getGenres()
                            .stream()
                            .map(GenreDtoMapper::mapToDto)
                            .collect(Collectors.toSet())
                    )
                    .likes(film.getLikes())
                    .review(film.getReview())
                    .build();
        }
    }
}
