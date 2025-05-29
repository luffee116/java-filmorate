package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Класс {@code ReviewDto} - DTO модель отзыва
 *
 * <p>Аннотация {@code @Data} автоматически генерирует геттеры, сеттеры, методы {@code equals()},
 * {@code hashCode()}, а также {@code toString()}. Аннотация {@code @NoArgsConstructor} автоматически генерирует
 * конструктор по умолчанию, который необходим при сериализации/десериализации объектов JSON. Аннотация {@code @Builder} создает Builder для создания объекта.</p>
 *
 * <p>Поля класса:</p>
 * <ul>
 *   <li>{@code id} - уникальный идентификатор отзыва. Может быть {@code null}</li>
 *   <li>{@code name} - текст отзыва</li>
 *   <li>{@code isPositive} - определение положительного/отрицательного отзыва. </li>
 *   <li>{@code userId} - id пользователя, добавившего отзыв. Не может быть {@code null} </li>
 *   <li>{@code filmId} - id фильма, к которому добавляется отзыв. Не может быть {@code null} </li>
 *   <li>{@code useful} - числовая переменная, отвечающая за полезность отзыва</li>
 * </ul>
 */

@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
public class ReviewDto {

    @NotNull(message = "id не может быть null")
    private Integer reviewId;

    @NotBlank
    private String content;

    private Boolean isPositive;

    @NotNull(message = "userId не может быть null")
    @Min(value = 1, message = "userId должен быть больше 1")
    private int userId;

    @NotNull(message = "filmId не может быть null")
    private int filmId;

    private Integer useful;
}