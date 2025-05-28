package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
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

    void delete(Integer id);

    boolean existsById(Integer id);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    Collection<Film> getFilmsDirector(Long filmId, String sortBy);

    List<Film> getPopularFilmsByGenreAndYear(int count, Integer genreId, Integer year);
}