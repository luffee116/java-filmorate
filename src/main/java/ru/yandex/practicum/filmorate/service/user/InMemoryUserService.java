package ru.yandex.practicum.filmorate.service.user;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@AllArgsConstructor
@Service
public class InMemoryUserService implements UserService {
    private final UserStorage userStorage;
    private static final Logger log = LoggerFactory.getLogger(InMemoryUserService.class);

    @Override
    public List<User> getAll() {
        log.info("Отправлен список всех пользователей, size:{}", userStorage.getAll().size());
        return userStorage.getAll();
    }

    @Override
    public User addUser(User user) {
        log.info("Добавлен новый пользователь с именем:{}", user.getName());
        return userStorage.addUser(user);
    }

    @Override
    public User updateUser(User user) {
        log.info("Пользователь с id:{}", user.getId());
        return userStorage.updateUser(user);
    }

    @Override
    public void addFriend(Integer firstId, Integer secondId) {
        userStorage.addFriend(firstId, secondId).orElseThrow(() -> {
            String message = String.format("Пользователь с id = %d или id = %d не найден", firstId, secondId);
            log.info(message);
            return new UserNotFoundException(message);
        });
    }

    @Override
    public void removeFriend(Integer firstId, Integer secondId) {
        userStorage.removeFriend(firstId, secondId).orElseThrow(() -> {
            String message = String.format("Пользователь с id = %d или id = %d не найден", firstId, secondId);
            log.info(message);
            return new UserNotFoundException(message);
        });
    }

    @Override
    public List<User> getCommonFriends(Integer firstId, Integer secondId) {
        return userStorage.getCommonFriends(firstId, secondId).orElseThrow(() -> {
            String message = String.format("Пользователь с id = %d или с id = %d не найден", firstId, secondId);
            log.info(message);
            return new UserNotFoundException(message);
        });
    }

    @Override
    public List<User> getUserFriends(Integer userId) {
        return userStorage.getUserFriends(userId).orElseThrow(() -> {
            String message = String.format("Пользователь с id = %d не найден", userId);
            log.info(message);
            return new UserNotFoundException(message);
        });
    }
}
