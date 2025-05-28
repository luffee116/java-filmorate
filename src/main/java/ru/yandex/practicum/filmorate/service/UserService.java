package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exeptions.FriendshipException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;

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
    private final UserFeedService userFeedService;

    public List<UserDto> getAll() {
        log.info("Отправлен список всех пользователей, size:{}", userStorage.getAll().size());
        List<User> users = userStorage.getAll();
        return users.stream().map(UserDtoMapper::mapToUserDto).toList();
    }

    public UserDto getUserById(Integer id) {
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserDtoMapper.mapToUserDto(user);
    }

    public UserDto addUser(UserDto user) {
        User user1 = UserMapper.mapToUser(user);
        if (user1.getName().isBlank()) {
            user1.setName(user1.getLogin());
        }
        User response = userStorage.addUser(user1);
        log.info("Добавлен новый пользователь с id:{}", response.getId());
        return UserDtoMapper.mapToUserDto(response);
    }

    public UserDto updateUser(UserDto user) {
        Optional<User> response = userStorage.updateUser(UserMapper.mapToUser(user));
        if (response.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id %s не найден", user.getId()));
        }
        log.info("Обновлен пользователь с id:{}", user.getId());
        return UserDtoMapper.mapToUserDto(response.get());
    }

    public void addFriend(Integer firstId, Integer secondId) {
        userStorage.addFriend(firstId, secondId)
                .orElseThrow(() -> new FriendshipException("Ошибка при добавлении в друзья"));
        userFeedService.createEvent(firstId, "FRIEND", "ADD", secondId);
        log.info("Добавлена дружба между пользователями с id {} и {}", firstId, secondId);
    }

    public void removeFriend(Integer firstId, Integer secondId) {
        userStorage.removeFriend(firstId, secondId)
                .orElseThrow(() -> new FriendshipException("Ошибка при удалении друзей"));
        userFeedService.createEvent(firstId, "FRIEND", "REMOVE", secondId);
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

    /**
     * Удаляет пользователя из хранилища.
     *
     * @param id идентификатор пользователя
     */
    @Transactional
    public void deleteUser(Integer id) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("Пользователь с идентификатором не найден: " + id);
        }
        userStorage.delete(id);
        log.info("Удален пользователя с помощью идентификатора: {}", id);
    }
}