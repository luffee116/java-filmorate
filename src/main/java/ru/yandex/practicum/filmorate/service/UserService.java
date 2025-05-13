package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.FriendshipException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class UserService {
    private final UserDbStorage userStorage;

    public List<User> getAll() {
        log.info("Отправлен список всех пользователей, size:{}", userStorage.getAll().size());
        return userStorage.getAll();
    }

    public User getUserById(Integer id) {
        log.info("Получение информации о пользователе с id {}", id);
        Optional<User> response = userStorage.getUserById(id);
        if (response.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователь с id {} не найден", id));
        }
        return response.get();
    }

    public User addUser(User user) {
        log.info("Добавлен новый пользователь с id:{}", user.getId());
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        Optional<User> response = userStorage.updateUser(user);
        if (response.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователь с id {} не найден", user.getId()));
        }
        log.info("Обновлен пользователь с id:{}", user.getId());
        return response.get();
    }

    public void addFriend(Integer firstId, Integer secondId) {
        userStorage.addFriend(firstId, secondId)
                .orElseThrow(() -> new FriendshipException("Ошибка при добавлении в друзья"));
        log.info("Добавлена дружба между пользователями с id {} и {}", firstId, secondId);
    }

    public void removeFriend(Integer firstId, Integer secondId) {
        userStorage.removeFriend(firstId, secondId)
                .orElseThrow(() -> new FriendshipException("Ошибка при удалении друзей"));
        log.info("Дружба удалена между пользователями с id {} и {}", firstId, secondId);
    }

    public List<User> getCommonFriends(Integer firstId, Integer secondId) {
        return userStorage.getCommonFriends(firstId, secondId)
                .orElseThrow(() -> new FriendshipException("Общие друзья не найдены"));
    }

    public List<User> getUserFriends(Integer userId) {
        return userStorage.getUserFriends(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}

