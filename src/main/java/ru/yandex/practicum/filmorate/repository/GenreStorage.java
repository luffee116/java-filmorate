package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {
    Genre getGenre(Integer id);

    Collection<Genre> getAllGenres();

    void setGenresForFilm(Film film); // Добавить жанры к фильму

    void clearGenresForFilm(int filmId); // Очистить жанры фильма
}
