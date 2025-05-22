package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Optional;
import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> getAll();

    Optional<Film> getById(Integer id);

    Optional<Boolean> addLike(Integer filmId, Integer userId);

    Optional<Boolean> removeLike(Integer filmId, Integer userId);

    List<Film> getPopularFilm(Integer count);

    List<Film> getCommonFilms(Integer userId, Integer friendId);
}
