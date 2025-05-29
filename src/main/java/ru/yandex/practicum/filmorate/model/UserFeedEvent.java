package ru.yandex.practicum.filmorate.model;

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
public class UserFeedEvent {
    private Long eventId;
    private Long timestamp;
    private int userId;
    private EventType eventType; // "LIKE", "REVIEW", "FRIEND" - вынес в enum EventType
    private EventOperation operation; // "REMOVE", "ADD", "UPDATE" - вынес в enum EventOperation
    private int entityId;
}