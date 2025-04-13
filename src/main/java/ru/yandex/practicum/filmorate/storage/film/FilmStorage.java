package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Optional;
import java.util.List;

public interface FilmStorage {

    Film create(Film Film);

    Film update(Film film);

    List<Film> getAll();

    Optional<Boolean> addLike(Integer filmId, Integer userId);

    Optional<Boolean> removeLike(Integer filmId, Integer userId);

    List<Film> getPopularFilm(Integer count);
}
