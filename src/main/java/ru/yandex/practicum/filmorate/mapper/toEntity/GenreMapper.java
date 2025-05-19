package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.Set;


public final class GenreMapper {
    public static Genre mapToGenre(GenreDto genreDto) {
        Genre genre = new Genre();
        genre.setId(genreDto.getId());
        genre.setName(genreDto.getName());

        return genre;
    }

    public static Set<Genre> mapToGenres(Set<GenreDto> genre) {
        Set<Genre> genres = new HashSet<>();
        for (GenreDto genreDto : genre) {
            genres.add(mapToGenre(genreDto));
        }
        return genres;
    }
}
