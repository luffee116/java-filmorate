package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest {

    @Autowired
    FilmController filmController;


    @Test
    void validateFilm_ShouldAccept200Description() throws NoSuchMethodException {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(200))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldAcceptPositiveDuration() throws NoSuchMethodException {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(200))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1L)
                .build();

        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldAcceptLimitReleaseDate() throws NoSuchMethodException {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(200))
                .releaseDate(LocalDate.of(1950, 12, 28))
                .duration(10L)
                .build();

        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldAcceptWhenValidFilm() throws NoSuchMethodException {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("ValidDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(10L)
                .build();

        assertDoesNotThrow(() -> filmController.addFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldThrowWhenNameIsEmpty() throws NoSuchMethodException {
        Film invalidFilm = Film.builder()
                .name(" ")
                .description("ValidDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(10L)
                .build();

        assertThrows(ValidationException.class, () -> filmController.addFilm(invalidFilm));
    }

    @Test
    void validateFilm_ShouldThrowLimitReleaseDate() {
        Film invalidFilm = Film.builder()
                .name(" ")
                .description("ValidDescription")
                .releaseDate(LocalDate.of(1950, 12, 27))
                .duration(10L)
                .build();
        assertThrows(ValidationException.class, () -> filmController.addFilm(invalidFilm));
    }

    @Test
    void validateFilm_ShouldThrow201Description() {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(201))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertThrows(ValidationException.class, () -> filmController.addFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldThrowNegativeDuration() {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(200))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-1L)
                .build();

        assertThrows(ValidationException.class, () -> filmController.addFilm(validFilm));
    }
}
