package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Builder(toBuilder = true)
@Data
public class Film {
    int id;
    String name;
    String description;
    LocalDate releaseDate;
    Long duration;
}

