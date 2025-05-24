package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.mapper.dto.EventDtoMapper;
import ru.yandex.practicum.filmorate.model.UserFeedEvent;
import ru.yandex.practicum.filmorate.repository.impl.UserFeedDbStorage;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFeedService {

    private final UserFeedDbStorage userFeedRepository;

    public void createEvent(Integer userId, String eventType, String operation, int entityId) {
        UserFeedEvent event = UserFeedEvent.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();

        userFeedRepository.addEvent(event);
    }

    public List<EventDto> getFeedByUserId(int userId) {
        List<UserFeedEvent> userFeed = userFeedRepository.getFeedByUserId(userId);
        return userFeed.stream().map(EventDtoMapper::mapToEventDto).toList();
    }
}