package ru.yandex.practicum.filmorate.storage.user;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Component
public class InMemoryUserStorage implements UserStorage {
    HashMap<Integer, User> usersStorage = new HashMap<>();
    int id = 1;

    @Override
    public List<User> getAll() {
        return usersStorage.values().stream().toList();
    }

    @Override
    public User addUser(User requestUser) {
        validateUser(requestUser);

        checkName(requestUser);
        requestUser.setId(generateId());
        usersStorage.put(requestUser.getId(), requestUser);
        return requestUser;
    }

    @Override
    public User updateUser(User requestUser) {
        validateUser(requestUser);

        if (!usersStorage.containsKey(requestUser.getId())) {
            throw new UserNotFoundException("Не найден пользователь с id: " + requestUser.getId());
        }

        checkName(requestUser);
        usersStorage.put(requestUser.getId(), requestUser);
        return requestUser;
    }

    @Override
    public Optional<Boolean> addFriend(Integer firstId, Integer secondId) {
        if (usersStorage.containsKey(firstId) && usersStorage.containsKey(secondId)) {
            User firstUser = usersStorage.get(firstId);
            firstUser.addFriend(secondId);
            updateUser(firstUser);

            User secondUser = usersStorage.get(secondId);
            secondUser.addFriend(firstId);
            updateUser(secondUser);

            return Optional.of(true);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> removeFriend(Integer firstId, Integer secondId) {
        if (usersStorage.containsKey(firstId) && usersStorage.containsKey(secondId)) {
            User firstUser = usersStorage.get(firstId);
            firstUser.removeFriend(secondId);
            updateUser(firstUser);

            User secondUser = usersStorage.get(secondId);
            secondUser.removeFriend(firstId);
            updateUser(secondUser);

            return Optional.of(true);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<User>> getCommonFriends(Integer firstUser, Integer secondUser) {
        if (usersStorage.containsKey(firstUser) && usersStorage.containsKey(secondUser)) {
            Set<Integer> user = usersStorage.get(firstUser).getFriends();
            Set<Integer> friend = usersStorage.get(secondUser).getFriends();
            List<User> usersList = user.stream()
                    .filter(friend::contains)
                    .map(usersStorage::get)
                    .toList();

            return Optional.of(usersList);
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<User>> getUserFriends(Integer userId) {
        if (usersStorage.containsKey(userId)) {
            return Optional.of(usersStorage
                    .get(userId)
                    .getFriends()
                    .stream()
                    .map(usersStorage::get)
                    .toList());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> checkUserId(Integer id) {
        if (usersStorage.containsKey(id)) {
            return Optional.of(true);
        }
        return Optional.empty();
    }

    private Integer generateId() {
        return id++;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
