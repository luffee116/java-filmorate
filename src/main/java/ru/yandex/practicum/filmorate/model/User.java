package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class User {
    int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private final Set<Integer> friends = new HashSet<>();


    public void addFriend(Integer friendId) {
        friends.add(friendId);
    }

    public void removeFriend(Integer friendId) {
        friends.remove(friendId);
    }
}
