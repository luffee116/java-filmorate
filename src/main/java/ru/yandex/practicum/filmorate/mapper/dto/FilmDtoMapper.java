package ru.yandex.practicum.filmorate.mapper.dto;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

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
                            .collect(Collectors.toSet())
                    )
                    .likes(film.getLikes())
                    .build();
        }
    }
}
