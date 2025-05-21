package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class UserDeletionTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserController userController;

    private UserDto testUser;

    @BeforeEach
    void setUp() {
        // Очищаем только таблицу users (так как тестируем только базовое удаление)
        jdbcTemplate.execute("DELETE FROM users");

        // Создаем тестового пользователя
        testUser = UserDto.builder()
                .email("test@example.com")
                .login("testLogin")
                .name("Test User")
                .birthday(LocalDate.now().minusYears(20))
                .build();
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() {
        // Добавляем пользователя
        UserDto addedUser = userController.addUser(testUser);

        // Проверяем, что пользователь добавлен (через счетчик записей в БД)
        Integer countBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class,
                addedUser.getId()
        );
        assertEquals(1, countBefore);

        // Удаляем пользователя
        userController.deleteUser(addedUser.getId());

        // Проверяем, что пользователь удален
        Integer countAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class,
                addedUser.getId()
        );
        assertEquals(0, countAfter);
    }

    @Test
    void deleteNonExistentUser_ShouldThrowNotFoundException() {
        // Пытаемся удалить несуществующего пользователя
        assertThrows(NotFoundException.class, () -> userController.deleteUser(9999));
    }

    @Test
    void deleteUser_ShouldReturnSuccessForExistingUser() {
        // Добавляем пользователя
        UserDto addedUser = userController.addUser(testUser);

        // Удаление должно завершиться без исключений
        assertDoesNotThrow(() -> userController.deleteUser(addedUser.getId()));
    }
}