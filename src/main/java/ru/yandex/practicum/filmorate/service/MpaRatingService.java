package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.mapper.dto.MpaDtoRatingMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.repository.impl.MpaRatingDbStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MpaRatingService {
    private final MpaRatingDbStorage mpaRatingDbStorage;

    public MpaRatingService(MpaRatingDbStorage mpaRatingDbStorage) {
        this.mpaRatingDbStorage = mpaRatingDbStorage;
    }

    public MpaDto getRating(Integer id) {
        log.info("Getting rating by id {}", id);
        MpaRating rating = mpaRatingDbStorage.getMpaRating(id);
        return MpaDtoRatingMapper.mapToDto(rating);
    }

    public Collection<MpaDto> getRatings() {
        log.info("Getting all ratings");
        Collection<MpaRating> ratings = mpaRatingDbStorage.getAllMpaRatings();
        return ratings.stream().map(MpaDtoRatingMapper::mapToDto).collect(Collectors.toList());
    }
}
