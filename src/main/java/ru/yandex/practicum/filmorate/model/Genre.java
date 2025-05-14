package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@AllArgsConstructor
public class Genre {
    private Integer id;
    private String name;
}
