package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedEvent {
    private Long eventId;
    private Long timestamp;
    private int userId;
    private String eventType; // "LIKE", "REVIEW", "FRIEND"
    private String operation; // "REMOVE", "ADD", "UPDATE"
    private int entityId;
}