package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedEvent {
    private int eventId;
    private long timestamp;
    private int userId;
    private String eventType; // "LIKE", "REVIEW", "FRIEND"
    private String operation; // "REMOVE", "ADD", "UPDATE"
    private int entityId;
}