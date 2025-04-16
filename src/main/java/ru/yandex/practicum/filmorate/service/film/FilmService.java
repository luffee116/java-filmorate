package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {

    Film create(Film requestFilm);

    Film update(Film requestFilm);

    List<Film> getAll();

    void addLike(Integer postId, Integer userId);

    void removeLike(Integer postId, Integer userId);

    List<Film> getPopularFilm(Integer count);
}
