package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exeptions.FilmUpdateException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.LikeException;
import ru.yandex.practicum.filmorate.mapper.dto.FilmDtoMapper;
import ru.yandex.practicum.filmorate.mapper.toEntity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.util.List;
import java.util.Set;

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
        log.info("Создание фильма с id: {}", film.getId());
        return FilmDtoMapper.mapToFilmDto(film);
    }

    @Transactional
    public FilmDto update(FilmDto filmDto) {
        // 1. Проверяем существование фильма
        if (!filmStorage.existsById(filmDto.getId())) {
            throw new FilmUpdateException("Cannot update non-existent film with id=" + filmDto.getId());
        }

        // 2. Обновляем фильм
        Film film = FilmMapper.mapToFilm(filmDto);
        Film updatedFilm = filmStorage.update(film);
        return FilmDtoMapper.mapToFilmDto(updatedFilm);
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

    public FilmDto getFilmById(Integer id) {
        Film film = filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Film not found"));
        return FilmDtoMapper.mapToFilmDto(film);
    }

    /**
     * Возвращает список фильмов, которые понравились как пользователю, так и его другу.
     * Результат отсортирован по убыванию популярности (количеству лайков).
     *
     * @param userId   идентификатор пользователя.
     * @param friendId идентификатор друга пользователя.
     * @return список DTO фильмов, которые понравились обоим пользователям,
     * отсортированных по популярности.
     * @throws NotFoundException если один из пользователей не найден.
     */
    public List<FilmDto> getCommonFilms(Integer userId, Integer friendId) {
        getExistingUser(userId);
        getExistingUser(friendId);
        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);
        log.info("Найдено {} общих фильмов для пользователей: userId={}, friendId={}", commonFilms.size(), userId, friendId);
        return commonFilms.stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }

    /**
     * Возвращает множество идентификаторов фильмов, которые были лайкнуты пользователем с указанным ID.
     *
     * @param userId идентификатор пользователя, для которого нужно получить лайки.
     * @return множество идентификаторов фильмов, лайкнутых данным пользователем.
     */
    public Set<Integer> getLikedFilmsIds(Integer userId) {
        return filmStorage.getLikedFilmsIds(userId);
    }

    public void addReview(Integer filmId, Integer userId, String text) {
        validateFilmAndUserId(filmId, userId);
        filmStorage.addReview(filmId, userId, text).orElseThrow(() -> new ReviewException("Ошибка при добавлении отзыва"));
        log.info("Добавлен отзыв для фильма id: {}, пользователем с id: {}", filmId, userId);

    }

    private void validateFilmAndUserId(Integer filmId, Integer userId) {
        if (filmStorage.getById(filmId).isEmpty()) throw new NotFoundException("Film not found");
        if (userStorage.getUserById(userId).isEmpty()) throw new NotFoundException("User not found");
    }

    /**
     * Удаляет фильм из хранилища.
     *
     * @param id идентификатор фильма
     */
    @Transactional
    public void deleteFilm(Integer id) {
        if (!filmStorage.existsById(id)) {
            throw new NotFoundException("Фильм с id не найден: " + id);
        }
        filmStorage.delete(id);
        log.info("Удаленный фильм с id: {}", id);
    }

    /**
     * Проверяет существование пользователя с указанным идентификатором.
     * <p>
     * Если пользователь с {@code userId} не найден, выбрасывается исключение {@link NotFoundException}.
     *
     * @param userId идентификатор пользователя для проверки.
     * @throws NotFoundException если пользователь с таким {@code userId} не существует.
     */
    private void getExistingUser(Integer userId) {
        userStorage.getUserById(userId).orElseThrow(() -> {
            String message = "Пользователь с id=" + userId + " не найден";
            log.warn(message);
            return new NotFoundException(message);
        });
    }
}