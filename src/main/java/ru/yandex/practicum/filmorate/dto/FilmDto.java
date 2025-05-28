package ru.yandex.practicum.filmorate.dto;


import jakarta.validation.ValidationException;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FilmDto {

    private int id;

    @NotBlank(message = "Name не должен быть пустой")
    @NotNull(message = "Name не должен быть null")
    private String name;

    @Size(max = 200, message = "Описание должно быть не более 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @Positive(message = "Длина фильма должна быть положительным числом")
    private Long duration;

    @NotNull(message = "Mpa не может быть пустой", groups = ValidationException.class)
    private MpaDto mpa;

    private Set<GenreDto> genres;

    private Set<Integer> likes;

    private Map<Integer, String> review;

    private Set<DirectorDto> directors;

    @AssertTrue
    public boolean isReleaseDateValid() {
        return releaseDate != null && releaseDate.isAfter(LocalDate.of(1950, 12, 28));
    }
}