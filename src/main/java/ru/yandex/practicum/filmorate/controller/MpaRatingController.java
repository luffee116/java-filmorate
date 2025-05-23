package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
public class MpaRatingController {
    private final MpaRatingService mpaRatingService;

    public MpaRatingController(MpaRatingService mpaRatingService) {
        this.mpaRatingService = mpaRatingService;
    }

    @GetMapping("/{id}")
    public MpaDto getMpaRating(@PathVariable Integer id) {
        return mpaRatingService.getRating(id);
    }

    @GetMapping
    public Collection<MpaDto> getAllMpaRatings() {
        return mpaRatingService.getRatings();
    }
}
