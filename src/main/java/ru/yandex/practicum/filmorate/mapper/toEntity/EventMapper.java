package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;

public final class EventMapper {

    public static UserFeedEvent mapToEvent(EventDto eventDto) {
        if (eventDto == null) {
            return null;
        }
        return UserFeedEvent.builder()
                .timestamp(eventDto.getTimestamp())
                .userId(eventDto.getUserId())
                .eventType(eventDto.getEventType())
                .operation(eventDto.getOperation())
                .eventId(eventDto.getEventId())
                .entityId(eventDto.getEntityId())
                .build();
    }
}