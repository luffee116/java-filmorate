package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {
    GenreService genresService;

    public GenreController(GenreService genreService) {
        this.genresService = genreService;
    }

    @GetMapping
    public List<GenreDto> getAllGenres() {
        return genresService.getGenres().stream().toList();
    }

    @GetMapping("/{id}")
    public GenreDto getGenreById(@PathVariable Integer id) {
        return genresService.getGenreById(id);
    }

}
