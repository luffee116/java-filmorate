package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Класс {@code Review} - модель отзыва
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

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Integer reviewId;
    private String content;
    private Boolean isPositive;
    private int userId;
    private int filmId;
    private Integer useful;
}
