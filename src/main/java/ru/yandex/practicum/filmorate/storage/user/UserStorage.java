package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> getAll();

    User addUser(User user);

    User updateUser(User requestUser);

    Optional<Boolean> addFriend(Integer firstId, Integer secondId);

    Optional<Boolean> removeFriend(Integer firstId, Integer secondId);

    Optional<List<User>> getCommonFriends(Integer firstUser, Integer secondUser);

    Optional<List<User>> getUserFriends(Integer userId);

    Optional<Boolean> checkUserId(Integer id);
}
