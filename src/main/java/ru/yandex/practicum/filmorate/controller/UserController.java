package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserFeedService;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;


@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserFeedService feedService;
    private final UserService userService;
    private final RecommendationService recommendationService;

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody UserDto requestUser) {
        return userService.addUser(requestUser);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserDto requestUser) {
        return userService.updateUser(requestUser);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(
            @PathVariable Integer userId,
            @PathVariable Integer friendId
    ) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFriend(
            @PathVariable Integer userId,
            @PathVariable Integer friendId
    ) {
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends/common/{friendId}")
    public List<UserDto> getCommonFriends(
            @PathVariable Integer userId,
            @PathVariable Integer friendId
    ) {
        return userService.getCommonFriends(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<UserDto> getUserFriends(@PathVariable Integer userId) {
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{userId}/feed")
    public List<EventDto> getUserFeed(@PathVariable Integer userId) {
        return feedService.getFeedByUserId(userId);
    }

    /**
     * Возвращает список рекомендованных фильмов для пользователя по его id.
     *
     * @param userId идентификатор пользователя
     * @return список DTO фильмов, рекомендованных для просмотра пользователем
     */
    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getUserRecommendations(@PathVariable("id") Integer userId) {
        return recommendationService.getRecommendations(userId);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }
}