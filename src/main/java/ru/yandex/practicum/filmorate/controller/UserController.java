package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;


@RestController
@RequestMapping("/users")
public class UserController {
    HashMap<Integer, User> usersStorage = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> getAll() {
        return usersStorage.values();
    }

    @PostMapping
    public User addUser(@RequestBody User requestUser) {
        validateUser(requestUser);

        if (requestUser.getName() == null || requestUser.getName().isBlank()) {
            requestUser.setName(requestUser.getLogin());
        }

        requestUser.setId(generateId());
        usersStorage.put(requestUser.getId(), requestUser);
        log.info("Добавлен новый пользователь {}", requestUser);
        return requestUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User requestUser) {
        validateUser(requestUser);

        if (!usersStorage.containsKey(requestUser.getId())) {
            log.error("Ошибка обновления: пользователь с id {} не найден", requestUser.getId());
            throw new UserNotFoundException("Не найден пользователь с id: " + requestUser.getId());
        }
        usersStorage.put(requestUser.getId(), requestUser);
        log.info("Обновлен польз`ователь с id {}: {}", requestUser.getId(), requestUser);
        return requestUser;
    }

    public void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка валидации: некорректная электронная почта {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: некорректный логин {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: некорректная дата рождения {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private Integer generateId() {
        int currentMaxId = usersStorage
                .keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
