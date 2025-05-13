package ru.yandex.practicum.filmorate.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FilmDto {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;
    private Set<Integer> likes;
}