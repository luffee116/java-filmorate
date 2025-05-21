package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.dto.FilmDtoMapper;
import ru.yandex.practicum.filmorate.mapper.toEntity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    FilmService filmService;
    FilmStorage filmStorage;

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
    public FilmDto update(FilmDto requestFilm) {
        // 1. Проверяем существование фильма
        if (!filmStorage.existsById(requestFilm.getId())) {
            throw new NotFoundException("Film with id=" + requestFilm.getId() + " not found");
        }
        // 2. Обновляем фильм
        Film updatedFilm = filmStorage.update(FilmMapper.mapToFilm(requestFilm));
        return FilmDtoMapper.mapToFilmDto(updatedFilm);
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
}
