package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.stream.Collectors;

public final class FilmMapper {
    public static Film mapToFilm(FilmDto filmDto) {
        return Film.builder()
                .id(filmDto.getId())
                .name(filmDto.getName())
                .description(filmDto.getDescription())
                .mpa(MpaRatingMapper.mapToRating(filmDto.getMpa()))
                .duration(filmDto.getDuration())
                .releaseDate(filmDto.getReleaseDate())
                .genres(filmDto.getGenres()
                        .stream()
                        .map(GenreMapper::mapToGenre)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static Film updateFilm(FilmDto filmDto, Film film) {
        if (!filmDto.getName().isBlank()) {
            film.setName(filmDto.getName());
        }
        if (!film.getDescription().isBlank()) {
            film.setDescription(film.getDescription());
        }
        if (film.getMpa() != null) {
            film.setMpa(film.getMpa());
        }
        if (film.getDuration() != null) {
            film.setDuration(film.getDuration());
        }
        if (film.getGenres() != null) {
            film.setGenres(film.getGenres());
        }
        return film;
    }
}
