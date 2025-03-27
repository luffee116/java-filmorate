package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Test
    void validateFilm_ShouldAccept200Description() {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(200))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertDoesNotThrow(() -> filmController.validateFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldAcceptPositiveDuration() {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(200))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0L)
                .build();

        assertDoesNotThrow(() -> filmController.validateFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldAcceptLimitReleaseDate() {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(200))
                .releaseDate(LocalDate.of(1950, 12, 28))
                .duration(10L)
                .build();
        assertDoesNotThrow(() -> filmController.validateFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldAcceptWhenValidFilm() {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("ValidDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(10L)
                .build();
        assertDoesNotThrow(() -> filmController.validateFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldThrowWhenNameIsEmpty() {
        Film unvalidFilm = Film.builder()
                .name(" ")
                .description("ValidDescription")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(10L)
                .build();
        assertThrows(ValidationException.class, () -> filmController.validateFilm(unvalidFilm));
    }

    @Test
    void validateFilm_ShouldThrowLimitReleaseDate() {
        Film unvalidFilm = Film.builder()
                .name(" ")
                .description("ValidDescription")
                .releaseDate(LocalDate.of(1950, 12, 27))
                .duration(10L)
                .build();
        assertThrows(ValidationException.class, () -> filmController.validateFilm(unvalidFilm));
    }

    @Test
    void validateFilm_ShouldThrow201Description() {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(201))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120L)
                .build();

        assertThrows(ValidationException.class, () -> filmController.validateFilm(validFilm));
    }

    @Test
    void validateFilm_ShouldThrowNegativeDuration() {
        Film validFilm = Film.builder()
                .name("Valid Film")
                .description("q".repeat(200))
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-1L)
                .build();

        assertThrows(ValidationException.class, () -> filmController.validateFilm(validFilm));
    }
}
