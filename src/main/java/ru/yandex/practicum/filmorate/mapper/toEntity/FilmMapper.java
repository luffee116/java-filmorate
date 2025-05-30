package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.stream.Collectors;

public class FilmMapper {
    public static Film mapToFilm(FilmDto filmDto) {
        Film film = new Film();

        film.setId(filmDto.getId());
        film.setName(filmDto.getName());
        film.setDescription(filmDto.getDescription());
        film.setReleaseDate(filmDto.getReleaseDate());
        film.setDuration(filmDto.getDuration());
        film.setMpa(MpaRatingMapper.mapToRating(filmDto.getMpa()));
        film.setGenres(filmDto.getGenres().stream()
                        .map(GenreMapper::mapToGenre)
                        .collect(Collectors.toSet()));
        film.setDirectors(filmDto.getDirectors().stream()
                .map(DirectorMapper::toDirector)
                .collect(Collectors.toSet()));
        film.setLikes(filmDto.getLikes());

        return film;
    }
}
