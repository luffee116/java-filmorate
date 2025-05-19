package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;


@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    UserService userService;

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
}
