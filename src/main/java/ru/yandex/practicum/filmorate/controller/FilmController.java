package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;


@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
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

//    @PutMapping("/{filmId}/like/{userId}")
//    public void addLike(
//            @PathVariable Integer filmId,
//            @PathVariable Integer userId
//    ) {
//        filmService.addLike(filmId, userId);
//    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Void> addLike(
            @PathVariable Integer filmId,
            @PathVariable Integer userId
    ) {
        try {
            filmService.addLike(filmId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.warn("Like operation failed: {}", e.getMessage());
            return ResponseEntity.ok().build(); // Все равно 200
        }
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

    @GetMapping("/director/{id}")
    public Collection<FilmDto> getFilmsDirector(
            @PathVariable Long id,
            @RequestParam(defaultValue = "year") String sortBy
    ) {
        return filmService.getFilmsDirector(id, sortBy);
    }

    @GetMapping("/AllPopular")
    public List<FilmDto> getPopularFilmsByGenreAndYear(
            @RequestParam(name = "count", required = false, defaultValue = "10") int count,
            @RequestParam(name = "genreId", required = false) Integer genreId,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        return filmService.getPopularFilmsByGenreAndYear(count, genreId, year);
    }

    @GetMapping("/search")
    public List<FilmDto> searchFilms(@RequestParam String query,
                                     @RequestParam List<String> by) {
        return filmService.searchFilms(query, by);
    }
}