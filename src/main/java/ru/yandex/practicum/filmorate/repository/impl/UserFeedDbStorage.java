package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;
import ru.yandex.practicum.filmorate.repository.UserFeedStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserFeedDbStorage implements UserFeedStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_EVENT_QUERY = """
            INSERT INTO user_feed (timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String GET_FEED_BY_USER_ID_QUERY = """
            SELECT event_id, timestamp, uf.user_id, event_type, operation, entity_id
            FROM user_feed uf
            WHERE uf.user_id = ?

            UNION ALL

            SELECT event_id, timestamp, uf.user_id, event_type, operation, entity_id
            FROM user_feed uf
            WHERE uf.user_id IN (SELECT friend_id FROM user_friends WHERE user_id = ?)
            ORDER BY timestamp ASC
                        """;

    @Override
    public void addEvent(UserFeedEvent event) {
        jdbcTemplate.update(ADD_EVENT_QUERY, event.getTimestamp(), event.getUserId(), event.getEventType(), event.getOperation(), event.getEntityId());
    }

    @Override
    public List<UserFeedEvent> getFeedByUserId(int userId) {
        return jdbcTemplate.query(
                GET_FEED_BY_USER_ID_QUERY,
                (rs, rowNum) -> UserFeedEvent.builder()
                        .eventId(rs.getLong("event_id"))
                        .timestamp(rs.getLong("timestamp"))
                        .userId(rs.getInt("user_id"))
                        .eventType(rs.getString("event_type"))
                        .operation(rs.getString("operation"))
                        .entityId(rs.getInt("entity_id"))
                        .build(),
                userId,
                userId
        );
    }
}