package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

public final class UserMapper {
    public static User mapToUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .login(userDto.getLogin())
                .birthday(userDto.getBirthday())
                .friends(userDto.getFriendsId())
                .build();
    }
}
