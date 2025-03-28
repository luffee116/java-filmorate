package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    Map<Integer, Film> filmStorage = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Отправлен список всех фильмов");
        return filmStorage.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film requestFilm) {
        validateFilm(requestFilm);
        requestFilm.setId(generateId());
        filmStorage.put(requestFilm.getId(), requestFilm);
        log.info("Добавлен новый фильм {}", requestFilm);
        return requestFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film requestFilm) {
        validateFilm(requestFilm);

        if (filmStorage.containsKey(requestFilm.getId())) {
            filmStorage.put(requestFilm.getId(), requestFilm);
            log.info("Обновлен фильм с id {} : {}", requestFilm.getId(), requestFilm);
            return requestFilm;
        } else {
            log.error("Не найден фильм с id {}", requestFilm.getId());
            throw new FilmNotFoundException("Фильм с указанным id не найден");
        }
    }

    private Integer generateId() {
        int currentMaxId = filmStorage
                .keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateFilm(Film film) {
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: некорректная продолжительность фильма {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getName().isBlank()) {
            log.error("Ошибка валидации: некорректное название фильма {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (!film.getDescription().isBlank()) {
            if (film.getDescription().length() > 200) {
                log.error("Ошибка валидации: некорректное описание {}", film.getDescription());
                throw new ValidationException("Длина описания должна быть не больше 200 символов");
            }
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1950, 12, 28))) {
            log.error("Ошибка валидации: некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть ранее, чем 28 декабря 1950г.");
        }
    }
}
