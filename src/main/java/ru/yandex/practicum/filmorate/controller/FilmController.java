package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    FilmService filmService;

    @GetMapping
    public Collection<FilmDto> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto addFilm(@Valid @RequestBody FilmDto requestFilm) {
        return filmService.create(requestFilm);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto requestFilm) {
        return filmService.update(requestFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(
            @PathVariable Integer filmId,
            @PathVariable Integer userId
    ) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(
            @PathVariable Integer filmId,
            @PathVariable Integer userId
    ) {
        filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilm(@RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        return filmService.getPopularFilm(count);
    }

    /**
     * Удаляет фильм по идентификатору.
     *
     * @param id идентификатор фильма
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable Integer id) {
        filmService.deleteFilm(id);
    }

    /**
     * Возвращает список фильмов, которые понравились обоим пользователям — указанному пользователю и его другу.
     * Список отсортирован по убыванию популярности (количеству лайков).
     *
     * @param userId   идентификатор пользователя.
     * @param friendId идентификатор друга пользователя.
     * @return список DTO фильмов, которые оба пользователя отметили как понравившиеся,
     * отсортированных по популярности.
     */
    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(@RequestParam Integer userId,
                                        @RequestParam Integer friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    public List<FilmDto> searchFilms(@RequestParam String query,
                                     @RequestParam List<String> by) {
        return filmService.searchFilms(query, by);
    }
}