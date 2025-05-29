package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.model.enumeration.EventType;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long timestamp;
    private int userId;
    private EventType eventType;     // Тип события: LIKE, REVIEW, FRIEND
    private EventOperation operation;     // Операция: REMOVE, ADD, UPDATE
    private Long eventId;
    private int entityId;         // ID связанной сущности

}