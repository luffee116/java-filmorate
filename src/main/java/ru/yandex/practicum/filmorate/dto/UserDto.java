package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.*;
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
public class UserDto {
    private int id;

    @NotBlank(message = "Email обязателен", groups = ValidationException.class)
    @Email(message = "Email должен быть валидным", groups = ValidationException.class)
    @Pattern(regexp = ".+@.+\\..+", message = "Email должен быть в формате example@example.com", groups = ValidationException.class)
    private String email;

    @NotBlank(message = "Логин обязателен", groups = ValidationException.class)
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы", groups = ValidationException.class)
    private String login;
    private String name;

    @NotNull(message = "Дата рождения не может быть пустой",groups = ValidationException.class)
    @PastOrPresent(message = "Дата рождения не может быть в будущем", groups = ValidationException.class)
    private LocalDate birthday;
    private Set<Integer> friendsId;
}