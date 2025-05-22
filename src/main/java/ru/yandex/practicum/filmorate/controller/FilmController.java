package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserFeedService;

import java.util.*;

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
        return filmService.getFilmById(id).orElseThrow(() -> new NotFoundException("Not Found"));
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

    @PutMapping("/{filmId}/review/{userId}")
    public void addReview(@PathVariable Integer filmId,
                          @PathVariable Integer userId,
                          @RequestBody Map<String, String> text) {
        filmService.addReview(filmId, userId, text.get("text"));
    }
}