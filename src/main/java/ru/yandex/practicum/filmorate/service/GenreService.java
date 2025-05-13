package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public Optional<Genre> getGenreById(Integer id) {
        log.info("Отправлен жанр с id: {}", id);
        return genreStorage.getGenre(id);
    }

    public Collection<Genre> getGenres() {
        log.info("Отправлен список всех жанров");
        return genreStorage.getAllGenres();
    }
}
