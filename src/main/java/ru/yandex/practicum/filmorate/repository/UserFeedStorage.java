package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.UserFeedEvent;

import java.util.List;

public interface UserFeedStorage {

    void addEvent(UserFeedEvent event);

    List<UserFeedEvent> getFeedByUserId(int userId);
}