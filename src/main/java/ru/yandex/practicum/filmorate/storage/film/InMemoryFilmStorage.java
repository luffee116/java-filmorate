package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> filmStorage = new HashMap<>();
    private int id = 1;

    @Override
    public Film create(Film requestFilm) {
        validateFilm(requestFilm);
        requestFilm.setId(generateId());
        filmStorage.put(requestFilm.getId(), requestFilm);
        return requestFilm;
    }

    @Override
    public Film update(Film requestFilm) {
        validateFilm(requestFilm);

        if (filmStorage.containsKey(requestFilm.getId())) {
            filmStorage.put(requestFilm.getId(), requestFilm);
            return requestFilm;
        } else {
            throw new FilmNotFoundException("Фильм с id " + requestFilm.getId() + " не найден");
        }
    }

    @Override
    public List<Film> getAll() {
        return filmStorage.values().stream().toList();
    }

    @Override
    public Optional<Boolean> addLike(Integer filmId, Integer userId) {
        if (filmStorage.containsKey(filmId)) {
            Film film = filmStorage.get(filmId);
            film.addLike(userId);
            update(film);

            return Optional.of(true);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> removeLike(Integer filmId, Integer userId) {
        if (filmStorage.containsKey(filmId)) {
            Film film = filmStorage.get(filmId);
            if (film.getLikes().contains(userId)) {
                film.removeLike(userId);
                update(film);
                return Optional.of(true);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Film> getPopularFilm(Integer count) {
        return filmStorage.values().stream()
                .sorted(Comparator.comparing(film -> -film.getLikes().size()))
                .limit(count)
                .toList();
    }

    private Integer generateId() {
        return id++;
    }

    private void validateFilm(Film film) {
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (!film.getDescription().isBlank()) {
            if (film.getDescription().length() > 200) {
                throw new ValidationException("Длина описания должна быть не больше 200 символов");
            }
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1950, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть ранее, чем 28 декабря 1950г.");
        }
    }
}
