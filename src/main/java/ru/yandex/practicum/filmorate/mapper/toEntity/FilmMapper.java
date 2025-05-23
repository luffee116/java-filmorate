package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.stream.Collectors;

public final class FilmMapper {
    public static Film mapToFilm(FilmDto filmDto) {
        if (filmDto.getGenres() == null) {
            return Film.builder()
                    .id(filmDto.getId())
                    .name(filmDto.getName())
                    .description(filmDto.getDescription())
                    .mpa(MpaRatingMapper.mapToRating(filmDto.getMpa()))
                    .duration(filmDto.getDuration())
                    .releaseDate(filmDto.getReleaseDate())
                    .likes(filmDto.getLikes())
                    .build();
        } else {
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
                    .likes(filmDto.getLikes())
                    .build();
        }
    }

    public static Film updateFilm(FilmDto filmDto, Film film) {
        if (!filmDto.getName().isBlank()) {
            film.setName(filmDto.getName());
        }
        if (!filmDto.getDescription().isBlank()) {
            film.setDescription(filmDto.getDescription());
        }
        if (filmDto.getMpa() != null) {
            film.setMpa(MpaRatingMapper.mapToRating(filmDto.getMpa()));
        }
        if (filmDto.getDuration() != null) {
            film.setDuration(filmDto.getDuration());
        }
        if (filmDto.getGenres() != null) {
            film.setGenres(GenreMapper.mapToGenres(filmDto.getGenres()));
        }
        if (filmDto.getReleaseDate() != null) {
            film.setReleaseDate(filmDto.getReleaseDate());
        }
        return film;
    }
}
