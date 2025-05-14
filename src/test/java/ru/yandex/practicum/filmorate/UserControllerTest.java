package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

@SpringBootTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class})
public class UserControllerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserController userController;

    UserDto user;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM users");

        user = new UserDto();
        user.setId(1);
        user.setEmail("test@email.ru");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(2002,12,28));
    }

    @Test
    void testAddFilm() {
        UserDto created = userController.addUser(user);
        assertNotNull(created.getId());
        assertEquals("test@email.ru", created.getEmail());
        assertEquals("test", created.getLogin());
        assertEquals(LocalDate.of(2002,12,28), created.getBirthday());
    }

    @Test
    void testUpdateFilm() {
        UserDto created = userController.addUser(user);
        UserDto newUser = new UserDto();
        newUser.setId(created.getId());
        newUser.setLogin("UPDATED");
        newUser.setEmail(created.getEmail());
        newUser.setBirthday(LocalDate.of(1998,12,28));
        UserDto updated = userController.updateUser(newUser);
        assertEquals("UPDATED", updated.getLogin());
        assertEquals(LocalDate.of(1998,12,28), updated.getBirthday());
    }

    @Test
    void testGetALlUsers() {
        userController.addUser(user);
        userController.addUser(user);
        userController.addUser(user);

        assertEquals(3, userController.getAll().size());
    }

    @Test
    void validateUser_ShouldThrowWhenBlankEmail() {
        UserDto user = UserDto.builder()
                .email(" ")
                .login("login")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldTrowWhenWrongEmail() {
        UserDto user = UserDto.builder()
                .email("test.ya.ru")
                .login("login")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldThrowWhenBlankLogin() {
        UserDto user = UserDto.builder()
                .email("aliullov@mail.ru")
                .login("")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldThrowWhenSpaceInLogin() {
        UserDto user = UserDto.builder()
                .email("aliullov@mail.ru")
                .login("luffee kazan")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldThrowWhenBirthdayIsNull() {
        UserDto user = UserDto.builder()
                .email("aliullov@mail.ru")
                .login("luffee")
                .name("test")
                .birthday(null)
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldThrowWhenBirthdayAfterNow() {
        UserDto user = UserDto.builder()
                .email("aliullov@mail.ru")
                .login("luffee")
                .name("test")
                .birthday(LocalDate.of(2030, 12, 10))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldAcceptCorrectBirthday() {
        UserDto user = UserDto.builder()
                .email("aliullov@mail.ru")
                .login("luffee")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertDoesNotThrow(() -> userController.addUser(user));
    }


}
