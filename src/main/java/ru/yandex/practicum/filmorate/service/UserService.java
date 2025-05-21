package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exeptions.FriendshipException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.mapper.dto.UserDtoMapper;
import ru.yandex.practicum.filmorate.mapper.toEntity.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class UserService {
    private final UserDbStorage userStorage;

    public List<UserDto> getAll() {
        log.info("Отправлен список всех пользователей, size:{}", userStorage.getAll().size());
        List<User> users = userStorage.getAll();
        return users.stream().map(UserDtoMapper::mapToUserDto).toList();
    }

    public UserDto getUserById(Integer id) {
        log.info("Получение информации о пользователе с id {}", id);
        if (id > 0) {
            Optional<User> responseUser = userStorage.getUserById(id);
            if (responseUser.isEmpty()) {
                throw new UserNotFoundException(String.format("Пользователь с id %s не найден", id));
            }
            return UserDtoMapper.mapToUserDto(responseUser.get());
        } else {
            throw new UserNotFoundException(String.format("Пользователь с id %s недопустим или не найден", id));
        }
    }

    public UserDto addUser(UserDto user) {
        log.info("Добавлен новый пользователь с id:{}", user.getId());
        User user1 = UserMapper.mapToUser(user);
        User response = userStorage.addUser(user1);
        return UserDtoMapper.mapToUserDto(response);
    }

    public UserDto updateUser(UserDto user) {
        Optional<User> response = userStorage.updateUser(UserMapper.mapToUser(user));
        if (response.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователь с id %s не найден", user.getId()));
        }
        log.info("Обновлен пользователь с id:{}", user.getId());
        return UserDtoMapper.mapToUserDto(response.get());
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

    public List<UserDto> getCommonFriends(Integer firstId, Integer secondId) {
        List<User> users = userStorage.getCommonFriends(firstId, secondId)
                .orElseThrow(() -> new FriendshipException("Общие друзья не найдены"));
        return users.stream().map(UserDtoMapper::mapToUserDto).toList();
    }

    public List<UserDto> getUserFriends(Integer userId) {
        List<User> users = userStorage.getUserFriends(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return users.stream().map(UserDtoMapper::mapToUserDto).toList();
    }
}

