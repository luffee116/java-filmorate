package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {

    List<User> getAll();

    User addUser(User user);

    User updateUser(User user);

    void addFriend(Integer firstId, Integer secondId);

    void removeFriend(Integer firstId, Integer secondId);

    List<User> getCommonFriends(Integer firstId, Integer secondId);

    List<User> getUserFriends(Integer userId);


}
