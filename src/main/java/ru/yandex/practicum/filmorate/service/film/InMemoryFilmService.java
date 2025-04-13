package ru.yandex.practicum.filmorate.service.film;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@AllArgsConstructor
@Service
public class InMemoryFilmService implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmService.class);


    @Override
    public Film create(Film requestFilm) {
        return filmStorage.create(requestFilm);
    }

    @Override
    public Film update(Film requestFilm) {
        return filmStorage.update(requestFilm);
    }

    @Override
    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    @Override
    public void addLike(Integer postId, Integer userId) {
        userStorage.checkUserId(userId).orElseThrow(() -> {
            final String message = String.format("Пользователь с id= %d не найден", userId);
            log.info(message);

            return new UserNotFoundException(message);
        });

        filmStorage.addLike(postId, userId).orElseThrow(() -> {
            final String message = String.format("Фильм с id= %d не найден", postId);
            log.info(message);
            return new FilmNotFoundException(message);
        });
    }

    @Override
    public void removeLike(Integer postId, Integer userId) {
        userStorage.checkUserId(userId).orElseThrow(() -> {
            final String message = String.format("Пользователь с id= %d не найден", userId);
            log.info(message);
            return new UserNotFoundException(message);
        });

        filmStorage.removeLike(postId, userId).orElseThrow(() -> {
            final String message = String.format("Фильм с id= %d не найден", postId);
            log.info(message);
            return new FilmNotFoundException(message);
        });
    }

    @Override
    public List<Film> getPopularFilm(Integer count) {
        return filmStorage.getPopularFilm(count);
    }
}
