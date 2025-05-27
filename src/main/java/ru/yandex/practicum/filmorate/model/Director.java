package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director {
    private Integer id;
    @NotBlank(message = "Имя режиссёра не должно быть пустым")
    @Size(min = 1, max = 255, message = "Имя режиссёра должно быть от 1 до 255 символов")
    private String name;
}