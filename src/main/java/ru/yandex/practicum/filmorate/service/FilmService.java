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
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.impl.DirectorDbStorage;
import ru.yandex.practicum.filmorate.repository.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FilmService {
    private final FilmDbStorage filmDbStorage;
    private final FilmStorage filmStorage;
    private final UserDbStorage userStorage;
    private final UserFeedService userFeedService;
    private final DirectorDbStorage directorDbStorage;

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    @Autowired
    public FilmService(FilmDbStorage filmStorage, UserDbStorage userStorage, UserFeedService userFeedService,
                       FilmDbStorage filmDbStorage, DirectorDbStorage directorDbStorage) {
        this.filmDbStorage = filmDbStorage;
        this.userStorage = userStorage;
        this.userFeedService = userFeedService;
        this.filmStorage = filmStorage;
        this.directorDbStorage = directorDbStorage;
    }

    public FilmDto create(FilmDto requestFilm) {
        Film request = FilmMapper.mapToFilm(requestFilm);
        Film film = filmDbStorage.create(request);
        log.info("Создание фильма с id: {}", requestFilm.getId());
        return FilmDtoMapper.mapToFilmDto(film);
    }

    @Transactional
    public FilmDto update(FilmDto filmDto) {
        // 1. Проверяем существование фильма
        if (!filmDbStorage.existsById(filmDto.getId())) {
            throw new FilmUpdateException("Cannot update non-existent film with id=" + filmDto.getId());
        }

        // 2. Обновляем фильм
        Film film = FilmMapper.mapToFilm(filmDto);
        Film updatedFilm = filmDbStorage.update(film);
        return FilmDtoMapper.mapToFilmDto(updatedFilm);
    }

    public List<FilmDto> getPopularFilmsByGenreAndYear(int count, Integer genreId, Integer year) {
        List<Film> popularFilms = filmStorage.getPopularFilmsByGenreAndYear(count, genreId, year);
        return popularFilms.stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }

    public List<FilmDto> getAll() {
        log.info("Отправлен список всех фильмов, size: {}", filmDbStorage.getAll().size());
        List<Film> films = filmDbStorage.getAll();
        return films.stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }

    public void addLike1(Integer filmId, Integer userId) {
        validateFilmAndUserId(filmId, userId);
        filmDbStorage.addLike(filmId, userId);//.orElseThrow(() -> new LikeException("Ошибка при добавлении лайка"));
        userFeedService.createEvent(userId, "LIKE", "ADD", filmId);
        log.info("Добавлен лайка для фильма id: {}, пользователем с id: {}", filmId, userId);
    }

    public void addLike(Integer filmId, Integer userId) {
        validateFilmAndUserId(filmId, userId);

        Optional<Boolean> likeResult = filmDbStorage.addLike(filmId, userId);
        if (likeResult.isPresent() && likeResult.get()) {  // Если лайк успешно добавлен
            userFeedService.createEvent(userId, "LIKE", "ADD", filmId);
            log.info("Добавлен лайк для фильма id: {}, пользователем с id: {}", filmId, userId);
        } else {
            log.info("Лайк от пользователя {} фильму {} уже существует или не добавлен", userId, filmId);
        }
    }

    public void removeLike(Integer filmId, Integer userId) {
        validateFilmAndUserId(filmId, userId);
        filmDbStorage.removeLike(filmId, userId).orElseThrow(() -> new LikeException("Ошибка при удалении лайка"));
        userFeedService.createEvent(userId, "LIKE", "REMOVE", filmId);
        log.info("Удален лайк для фильма с id: {}, пользователем с id: {}", filmId, userId);
    }

    public List<FilmDto> getPopularFilm(Integer count) {
        List<Film> popularFilms = filmDbStorage.getPopularFilm(count);
        log.info("Отправлен список популярных фильмов, count: {}", count);
        return popularFilms.stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }

    public FilmDto getFilmById(Integer id) {
        Film film = filmDbStorage.getById(id)
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
        List<Film> commonFilms = filmDbStorage.getCommonFilms(userId, friendId);
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
        return filmDbStorage.getLikedFilmsIds(userId);
    }

    private void validateFilmAndUserId(Integer filmId, Integer userId) {
        if (filmDbStorage.getById(filmId).isEmpty()) throw new NotFoundException("Film not found");
        if (userStorage.getUserById(userId).isEmpty()) throw new NotFoundException("User not found");
    }

    /**
     * Удаляет фильм из хранилища.
     *
     * @param id идентификатор фильма
     */
    @Transactional
    public void deleteFilm(Integer id) {
        if (!filmDbStorage.existsById(id)) {
            throw new NotFoundException("Фильм с id не найден: " + id);
        }
        filmDbStorage.delete(id);
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

    /**
     * Отправляет список фильмов по подстроке
     * <p>
     *
     * @param stringForSearch подстрока для поиска
     * @param by              параметры поиска
     */
    public List<FilmDto> searchFilms(String stringForSearch, List<String> by) {
        if (by.isEmpty()) {
            throw new RuntimeException("Параметры поиска не указаны");
        }
        List<Film> response = filmStorage.search(stringForSearch, by);
        return filmStorage.search(stringForSearch, by)
                .stream()
                .map(FilmDtoMapper::mapToFilmDto)
                .toList();
    }

    public Collection<FilmDto> getFilmsDirector(Long id, String sortBy) {
        directorDbStorage.existById(id);
        return filmStorage.getFilmsDirector(id, sortBy).stream().map(FilmDtoMapper::mapToFilmDto).toList();
    }
}