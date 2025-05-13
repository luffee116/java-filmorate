package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exeptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.LikeException;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class FilmService {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    @Autowired
    public FilmService(FilmDbStorage filmStorage, UserDbStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(@Valid Film requestFilm) {
        Film film = filmStorage.create(requestFilm);
        log.info("Создание фильма с id: {}", requestFilm.getId());
        return film;
    }

    public Film update(@Valid Film requestFilm) {
        log.info("Обновлен фильм фильм с id: {}", requestFilm.getId());
        return filmStorage.update(requestFilm);
    }

    public List<Film> getAll() {
        log.info("Отправлен список всех фильмов, size: {}", filmStorage.getAll().size());
        return filmStorage.getAll();
    }

    public void addLike(Integer filmId, Integer userId) {
        validateFilmAndUserId(filmId, userId);
        filmStorage.addLike(filmId, userId).orElseThrow(() -> new LikeException("Ошибка при добавлении лайка"));
        log.info("Добавлен лайка для фильма id: {}, пользователем с id: {}", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validateFilmAndUserId(filmId, userId);
        filmStorage.removeLike(filmId, userId).orElseThrow(() -> new LikeException("Ошибка при удалении лайка"));
        log.info("Удален лайк для фильма с id: {}, пользователем с id: {}", filmId, userId);
    }

    public List<Film> getPopularFilm(Integer count) {
        List<Film> popularFilms = filmStorage.getPopularFilm(count);
        log.info("Отправлен список популярных фильмов, count: {}", count);
        return popularFilms;
    }

    public Optional<Film> getFilmById(Integer id) {
        Optional<Film> film = filmStorage.getById(id);
        log.info("Отправлен фильм с id: {}", id);
        return film;
    }

    private void validateFilmAndUserId(Integer filmId, Integer userId) {
        if (filmStorage.getById(filmId).isEmpty()) throw new FilmNotFoundException("Film not found");
        if (userStorage.getUserById(userId).isEmpty()) throw new UserNotFoundException("User not found");
    }
}
