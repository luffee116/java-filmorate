package ru.yandex.practicum.filmorate.rowMappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;
import ru.yandex.practicum.filmorate.model.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.model.enumeration.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.yandex.practicum.filmorate.model.enumeration.EnumUtils.safeValueOf;


public class UserFeedEventRowMapper implements RowMapper<UserFeedEvent> {
    @Override
    public UserFeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserFeedEvent.builder()
                .eventId(rs.getLong("event_id"))
                .timestamp(rs.getLong("timestamp"))
                .userId(rs.getInt("user_id"))
                .eventType(safeValueOf(EventType.class, rs.getString("event_type")))
                .operation(safeValueOf(EventOperation.class, rs.getString("operation")))
                .entityId(rs.getInt("entity_id"))
                .build();
    }

}