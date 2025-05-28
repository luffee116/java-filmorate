package ru.yandex.practicum.filmorate.repository.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.toEntity.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.TypeEntity;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.rowMappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
public class UserDbStorage extends BaseDbStorage implements UserStorage {
    private final UserRowMapper userRowMapper = new UserRowMapper();

    // SQL Запросы –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    private static final String ADD_USER_QUERY = """
            INSERT INTO users (EMAIL, LOGIN, NAME, BIRTHDAY)
            VALUES (?, ?, ?, ?);
            """;
    private static final String GET_ALL_USER_QUERY = """
            SELECT *
            FROM users;
            """;
    private static final String GET_USER_BY_ID_QUERY = """
            SELECT *
            FROM users
            WHERE id = ?;
            """;
    private static final String UPDATE_USER_QUERY = """
            UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?;
            """;
    private static final String ADD_FRIEND_QUERY = """
            INSERT INTO USER_FRIENDS (USER_ID, FRIEND_ID) VALUES (?, ?);
            """;
    private static final String DELETE_USER_BY_ID_QUERY = """
            DELETE FROM USER_FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?;
            """;
    private static final String USER_FRIENDS_FIND_REQUEST = """
            SELECT COUNT(*)
            FROM user_friends
            WHERE user_id = ? AND friend_id = ?;
            """;
    private static final String FIND_USER_FRIENDS_ID_LIST_BY_USER_ID = """
            SELECT friend_id
            FROM user_friends
            WHERE user_id = ?;
            """;
    private static final String GET_USER_COMMON_FRIENDS = """
            SELECT u.* FROM users u
            JOIN user_friends uf1 ON u.id = uf1.friend_id
            JOIN user_friends uf2 ON u.id = uf2.friend_id
            WHERE uf1.user_id = ? AND uf2.user_id = ?;
            """;
    private static final String FIND_USER_FRIENDS_LIST_BY_USER_ID = """
            SELECT u.* FROM users u
            JOIN user_friends uf ON u.id = uf.friend_id
            WHERE uf.user_id = ?;
            """;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // Вывод всех пользователей ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public List<User> getAll() {
        List<UserDto> users = jdbcTemplate.query(GET_ALL_USER_QUERY, userRowMapper);

        users.forEach(this::addFriendsToUserResponse);

        return users.stream().map(UserMapper::mapToUser).toList();
    }

    // Вывод пользователя по Id ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<User> getUserById(Integer id) {
        Objects.requireNonNull(id, "Id не может быть null");

        try {
            UserDto userDto = jdbcTemplate.queryForObject(GET_USER_BY_ID_QUERY, userRowMapper, id);
            loadFriends(id);
            return Optional.of(UserMapper.mapToUser(userDto));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    // Добавление пользователя –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public User addUser(User user) {
        validateRequestUser(user);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(ADD_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            if (user.getBirthday() != null) {
                ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    // Обновление пользователя –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<User> updateUser(User requestUser) {
        validateRequestUser(requestUser);
        checkUserId(requestUser.getId());

        int updatedRows = jdbcTemplate.update(UPDATE_USER_QUERY,
                requestUser.getEmail(),
                requestUser.getLogin(),
                requestUser.getName(),
                requestUser.getBirthday(),
                requestUser.getId());

        if (updatedRows > 0) {
            return Optional.of(requestUser);
        }
        log.warn("Failed to update user with id: {}", requestUser.getId());
        return Optional.empty();
    }

    // Добавление друга ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<Boolean> addFriend(Integer firstId, Integer secondId) {
        // Проверка существования пользователей
        checkUserId(firstId);
        checkUserId(secondId);

        int count = jdbcTemplate.queryForObject(USER_FRIENDS_FIND_REQUEST, Integer.class, firstId, secondId);

        // Проверка существования дружбы
        if (count > 0) {
            log.warn("Между пользователями уже существует дружба: {}, {}", firstId, secondId);
            return Optional.of(false);
        }

        // Добавление дружбы
        int rowsCount = jdbcTemplate.update(ADD_FRIEND_QUERY, firstId, secondId);
        return Optional.of(rowsCount > 0);
    }

    // Удаление дружбы ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-
    @Override
    public Optional<Boolean> removeFriend(Integer firstId, Integer secondId) {
        checkUserId(firstId);
        checkUserId(secondId);

        int rows = jdbcTemplate.update(DELETE_USER_BY_ID_QUERY, firstId, secondId);
        return Optional.of(rows > 0);
    }

    // Список общих друзей –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<List<User>> getCommonFriends(Integer firstUser, Integer secondUser) {

        List<UserDto> commonFriends = jdbcTemplate.query(GET_USER_COMMON_FRIENDS, userRowMapper, firstUser, secondUser);

        commonFriends.forEach(this::addFriendsToUserResponse);

        return Optional.of(commonFriends
                .stream()
                .map(UserMapper::mapToUser)
                .toList());
    }

    @Override
    public Optional<List<User>> getUserFriends(Integer userId) {
        checkUserId(userId);

        List<UserDto> friends = jdbcTemplate.query(FIND_USER_FRIENDS_LIST_BY_USER_ID, userRowMapper, userId);

        friends.forEach(this::addFriendsToUserResponse);

        return Optional.of(friends
                .stream()
                .map(UserMapper::mapToUser)
                .toList());
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        // Проверяем существование пользователя
        if (!existsById(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        // Удаляем связанные данные (лайки и дружеские связи)
        jdbcTemplate.update("DELETE FROM film_likes WHERE user_id = ?", id);
        jdbcTemplate.update("DELETE FROM user_friends WHERE user_id = ? OR friend_id = ?", id, id);

        // Удаляем пользователя
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    /**
     * Проверяет существование пользователя в базе данных по указанному идентификатору.
     * <p>
     * Использует эффективный запрос с EXISTS для минимальной нагрузки на базу данных.
     * Особенно полезно для проверок перед выполнением операций удаления или модификации.
     * </p>
     *
     * @param id идентификатор пользователя для проверки (должен быть не null)
     * @return true - если пользователь с указанным ID существует, false - если не существует
     * @throws IllegalArgumentException если переданный id равен null
     */
    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }
    // ВСПОМОГАТЕЛЬНЫЙ МЕТОДЫ

    // Проверка существования пользователя –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    private void checkUserId(Integer id) {
        checkEntityExist(id, TypeEntity.USER);
    }

    // Валидация –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    private void validateRequestUser(User user) {
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

    // Поиск id друзей пользователя ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    private Set<Integer> loadFriends(Integer userId) {
        return new HashSet<>(jdbcTemplate.queryForList(FIND_USER_FRIENDS_ID_LIST_BY_USER_ID, Integer.class, userId));
    }

    // Преобразование со списком друзей ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––

    private void addFriendsToUserResponse(UserDto userDto) {
        Integer userId = userDto.getId();
        userDto.setFriendsId(loadFriends(userId));
    }

}