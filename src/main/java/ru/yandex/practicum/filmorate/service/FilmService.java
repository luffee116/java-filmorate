package ru.yandex.practicum.filmorate.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exeptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.LikeException;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
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

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    @Autowired
    public FilmService(FilmDbStorage filmStorage, UserDbStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
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
        log.info("Добавлен лайка для фильма id: {}, пользователем с id: {}", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validateFilmAndUserId(filmId, userId);
        filmStorage.removeLike(filmId, userId).orElseThrow(() -> new LikeException("Ошибка при удалении лайка"));
        log.info("Удален лайк для фильма с id: {}, пользователем с id: {}", filmId, userId);
    }

    public List<FilmDto> getPopularFilm(Integer count) {
        List<Film> popularFilms = filmStorage.getPopularFilm(count);
        log.info("Отправлен список популярных фильмов, count: {}", count);
        return popularFilms.stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }

    /**
     * Возвращает список фильмов, которые понравились как пользователю, так и его другу.
     * Результат отсортирован по убыванию популярности (количеству лайков).
     *
     * @param userId   идентификатор пользователя.
     * @param friendId идентификатор друга пользователя.
     * @return список DTO фильмов, которые понравились обоим пользователям,
     * отсортированных по популярности.
     * @throws UserNotFoundException если один из пользователей не найден.
     * @throws ValidationException   если между пользователями нет дружбы.
     */
    public List<FilmDto> getCommonFilms(Integer userId, Integer friendId) {
        getExistingUser(userId);
        getExistingUser(friendId);
        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        log.info("Найдено {} общих фильмов для пользователей: userId={}, friendId={}", commonFilms.size(), userId, friendId);
        return commonFilms.stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }

    public Optional<FilmDto> getFilmById(Integer id) {
        Optional<Film> film = filmStorage.getById(id);
        log.info("Отправлен фильм с id: {}", id);
        return Optional.of(FilmDtoMapper.mapToFilmDto(film.get()));
    }

    private void validateFilmAndUserId(Integer filmId, Integer userId) {
        if (filmStorage.getById(filmId).isEmpty()) throw new FilmNotFoundException("Film not found");
        if (userStorage.getUserById(userId).isEmpty()) throw new UserNotFoundException("User not found");
    }

    /**
     * Проверяет существование пользователя с указанным идентификатором.
     * <p>
     * Если пользователь с {@code userId} не найден, выбрасывается исключение {@link UserNotFoundException}.
     *
     * @param userId идентификатор пользователя для проверки.
     * @throws UserNotFoundException если пользователь с таким {@code userId} не существует.
     */
    private void getExistingUser(Integer userId) {
        userStorage.getUserById(userId).orElseThrow(() -> {
            String message = "Пользователь с id=" + userId + " не найден";
            log.warn(message);
            return new UserNotFoundException(message);
        });
    }
}
