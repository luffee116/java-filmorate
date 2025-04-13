package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.*;

@AllArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    FilmService filmService;

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film requestFilm) {
        return filmService.create(requestFilm);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film requestFilm) {
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
    public List<Film> getPopularFilm(@RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        return filmService.getPopularFilm(count);
    }
}
