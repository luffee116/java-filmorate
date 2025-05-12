package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import jdk.jfr.BooleanFlag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Film {
    @NotNull(message = "Id не может быть пустой")
    private Integer id;

    @NotBlank(message = "Name не должен быть пустой", groups = Default.class)
    @NotNull(message = "Name не должен быть null", groups = Default.class)
    private String name;

    @Size(max = 200, message = "Описание должно быть не более 200 символов", groups = Default.class)
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой", groups = Default.class)
    private LocalDate releaseDate;

    @Positive(message = "Длина фильма должна быть положительным числом", groups = Default.class)
    private Long duration;

    private Set<Genre> genres;

    @NotNull(message = "Mpa не может быть пустой", groups = Default.class)
    private MpaRating mpa;

    private final Set<Integer> likes = new HashSet<>();

    public void addLike(Integer userId) {
        likes.add(userId);
    }

    public void removeLike(Integer userId) {
        likes.remove(userId);
    }

    @AssertTrue
    public boolean isReleaseDateValid() {
        return releaseDate != null && releaseDate.isAfter(LocalDate.of(1950, 12, 28));
    }
}

