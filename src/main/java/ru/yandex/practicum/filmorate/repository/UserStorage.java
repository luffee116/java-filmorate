package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> getAll();

    Optional<User> getUserById(Integer id);

    User addUser(User user);

    Optional<User> updateUser(User requestUser);

    Optional<Boolean> addFriend(Integer firstId, Integer secondId);

    Optional<Boolean> removeFriend(Integer firstId, Integer secondId);

    Optional<List<User>> getCommonFriends(Integer firstUser, Integer secondUser);

    Optional<List<User>> getUserFriends(Integer userId);

    void delete(Integer id);

    boolean existsById(Integer id);
}
