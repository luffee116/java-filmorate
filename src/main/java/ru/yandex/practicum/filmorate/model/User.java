package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

    @NotNull(message = "ID не может быть пустым")
    @Positive(message = "ID должен быть положительным числом")
    int id;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Email должен быть валидным")
    @Pattern(regexp = ".+@.+\\..+", message = "Email должен быть в формате example@example.com")
    private String email;

    @NotBlank(message = "Логин обязателен")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Integer> friends = new HashSet<>();

}
