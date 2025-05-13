package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.repository.impl.MpaRatingDbStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class MpaRatingService {
    private final MpaRatingDbStorage mpaRatingDbStorage;

    public MpaRatingService(MpaRatingDbStorage mpaRatingDbStorage) {
        this.mpaRatingDbStorage = mpaRatingDbStorage;
    }

    public Optional<MpaRating> getRating(Integer id) {
        log.info("Getting rating by id {}", id);
        return mpaRatingDbStorage.getMpaRating(id);
    }

    public Collection<MpaRating> getRatings() {
        log.info("Getting all ratings");
        return mpaRatingDbStorage.getAllMpaRatings();
    }
}
