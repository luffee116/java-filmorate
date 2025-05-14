package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

public interface MpaRatingStorage {
    MpaRating getMpaRating(Integer id);

    Collection<MpaRating> getAllMpaRatings();
}
