package ru.yandex.practicum.filmorate.mapper.dto;

import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;

public final class EventDtoMapper {

    public static EventDto mapToDto(final UserFeedEvent event) {
        if (event == null) {
            return null;
        }
        return EventDto.builder()
                .timestamp(event.getTimestamp())
                .userId(event.getUserId())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .eventId(event.getEventId())
                .entityId((int) event.getEntityId())
                .build();
    }
}