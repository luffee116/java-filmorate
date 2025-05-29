package ru.yandex.practicum.filmorate.mapper.dto;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class FilmDtoMapper {
    public static FilmDto mapToFilmDto(Film film) {
        if (film.getGenres() == null) {
            return FilmDto.builder()
                    .id(film.getId())
                    .name(film.getName())
                    .description(film.getDescription())
                    .releaseDate(film.getReleaseDate())
                    .duration(film.getDuration())
                    .mpa(MpaDtoRatingMapper.mapToDto(film.getMpa()))
                    .review(film.getReview())
                    .directors(film.getDirectors().stream()
                            .map(DirectorDtoMapper::toDirectorDto)
                            .collect(Collectors.toSet()))
                    .likes(film.getLikes())
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
                            .sorted(Comparator.comparing(GenreDto::getId))
                            .collect(Collectors.toCollection(LinkedHashSet::new)
                    ))
                    .directors(film.getDirectors().stream()
                            .map(DirectorDtoMapper::toDirectorDto)
                            .collect(Collectors.toSet()))
                    .likes(film.getLikes())
                    .review(film.getReview())
                    .build();
        }
    }
}
