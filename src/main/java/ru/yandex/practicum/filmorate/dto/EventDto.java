package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long timestamp;
    private int userId;
    private String eventType;     // Тип события: LIKE, REVIEW, FRIEND
    private String operation;     // Операция: REMOVE, ADD, UPDATE
    private Long eventId;
    private int entityId;         // ID связанной сущности

}