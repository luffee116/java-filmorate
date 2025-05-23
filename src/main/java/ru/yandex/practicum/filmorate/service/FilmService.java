package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exeptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.LikeException;
import ru.yandex.practicum.filmorate.exeptions.ReviewException;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.mapper.dto.FilmDtoMapper;
import ru.yandex.practicum.filmorate.mapper.toEntity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.util.List;
import java.util.Optional;

@Service
public class FilmService {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final UserFeedService userFeedService;

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    @Autowired
    public FilmService(FilmDbStorage filmStorage, UserDbStorage userStorage, UserFeedService userFeedService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.userFeedService = userFeedService;
    }

    public FilmDto create(FilmDto requestFilm) {
        Film request = FilmMapper.mapToFilm(requestFilm);
        Film film = filmStorage.create(request);
        log.info("Создание фильма с id: {}", requestFilm.getId());
        return FilmDtoMapper.mapToFilmDto(film);
    }

    public FilmDto update(FilmDto requestFilm) {
        Optional<Film> filmToUpdate = filmStorage.getById(requestFilm.getId());
        if (filmToUpdate.isPresent()) {
            Film film = filmStorage.update(FilmMapper.updateFilm(requestFilm, filmToUpdate.get()));
            log.info("Обновлен фильм фильм с id: {}", requestFilm.getId());
            return FilmDtoMapper.mapToFilmDto(film);
        }
        return null;
    }

    public List<FilmDto> getAll() {
        log.info("Отправлен список всех фильмов, size: {}", filmStorage.getAll().size());
        List<Film> films = filmStorage.getAll();
        return films.stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }

    public void addLike(Integer filmId, Integer userId) {
        validateFilmAndUserId(filmId, userId);
        filmStorage.addLike(filmId, userId).orElseThrow(() -> new LikeException("Ошибка при добавлении лайка"));
        userFeedService.createEvent(userId, "LIKE", "ADD", filmId);
        log.info("Добавлен лайка для фильма id: {}, пользователем с id: {}", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validateFilmAndUserId(filmId, userId);
        filmStorage.removeLike(filmId, userId).orElseThrow(() -> new LikeException("Ошибка при удалении лайка"));
        userFeedService.createEvent(userId, "LIKE", "REMOVE", filmId);
        log.info("Удален лайк для фильма с id: {}, пользователем с id: {}", filmId, userId);
    }

    public List<FilmDto> getPopularFilm(Integer count) {
        List<Film> popularFilms = filmStorage.getPopularFilm(count);
        log.info("Отправлен список популярных фильмов, count: {}", count);
        return popularFilms.stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }

    public Optional<FilmDto> getFilmById(Integer id) {
        if (id > 0) {
            Optional<Film> film = filmStorage.getById(id);
            log.info("Отправлен фильм с id: {}", id);
            return Optional.of(FilmDtoMapper.mapToFilmDto(film.get()));
        } else {
            throw new FilmNotFoundException(String.format("Фильм с id %s не найден:", id));
        }
    }

    public void addReview(Integer filmId, Integer userId, String text) {
        validateFilmAndUserId(filmId, userId);
        filmStorage.addReview(filmId, userId, text).orElseThrow(() -> new ReviewException("Ошибка при добавлении отзыва"));
        log.info("Добавлен отзыв для фильма id: {}, пользователем с id: {}", filmId, userId);

    }

    private void validateFilmAndUserId(Integer filmId, Integer userId) {
        if (filmStorage.getById(filmId).isEmpty()) throw new FilmNotFoundException("Film not found");
        if (userStorage.getUserById(userId).isEmpty()) throw new UserNotFoundException("User not found");
    }
}