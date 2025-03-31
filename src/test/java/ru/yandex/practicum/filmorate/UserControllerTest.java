package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    UserController userController;

    @Test
    void validateUser_ShouldThrowWhenBlankEmail() {
        User user = User.builder()
                .email(" ")
                .login("login")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldTrowWhenWrongEmail() {
        User user = User.builder()
                .email("test.ya.ru")
                .login("login")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldThrowWhenBlankLogin() {
        User user = User.builder()
                .email("aliullov@mail.ru")
                .login("")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldThrowWhenSpaceInLogin() {
        User user = User.builder()
                .email("aliullov@mail.ru")
                .login("luffee kazan")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldThrowWhenBirthdayIsNull() {
        User user = User.builder()
                .email("aliullov@mail.ru")
                .login("luffee")
                .name("test")
                .birthday(null)
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldThrowWhenBirthdayAfterNow() {
        User user = User.builder()
                .email("aliullov@mail.ru")
                .login("luffee")
                .name("test")
                .birthday(LocalDate.of(2030, 12, 10))
                .build();

        assertThrows(ValidationException.class, () -> userController.addUser(user));
    }

    @Test
    void validateUser_ShouldAcceptCorrectBirthday() {
        User user = User.builder()
                .email("aliullov@mail.ru")
                .login("luffee")
                .name("test")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        assertDoesNotThrow(() -> userController.addUser(user));
    }

    @Test
    void validatesUser_ShouldAddNameWhenEmpty() {
        User user = User.builder()
                .email("aliullov@mail.ru")
                .login("luffee")
                .name("")
                .birthday(LocalDate.of(2002, 11, 28))
                .build();

        userController.addUser(user);
        String expected = "luffee";
        String actual = user.getName();
        Assertions.assertEquals(expected, actual);
    }
}
