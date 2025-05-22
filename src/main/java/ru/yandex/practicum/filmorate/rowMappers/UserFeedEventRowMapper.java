package ru.yandex.practicum.filmorate.rowMappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserFeedEventRowMapper implements RowMapper<UserFeedEvent> {
    @Override
    public UserFeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserFeedEvent.builder()
                .eventId(rs.getInt("event_id"))
                .timestamp(rs.getLong("timestamp"))
                .userId(rs.getInt("user_id"))
                .eventType(rs.getString("event_type"))
                .operation(rs.getString("operation"))
                .entityId(rs.getInt("entity_id"))
                .build();
    }
}