package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mapper.dto.GenreDtoMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreDto getGenreById(Integer id) {
        log.info("Отправлен жанр с id: {}", id);
        Genre genre = genreStorage.getGenre(id);
        return GenreDtoMapper.mapToDto(genre);
    }

    public Collection<GenreDto> getGenres() {
        log.info("Отправлен список всех жанров");
        Collection<Genre> genres = genreStorage.getAllGenres();
        return genres.stream().map(GenreDtoMapper::mapToDto).collect(Collectors.toList());
    }
}
