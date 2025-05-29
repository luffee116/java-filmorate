package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class FilmDeletionTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmController filmController;

    private FilmDto testFilm;

    @BeforeEach
    void setUp() {
        // Очищаем связанные таблицы
        jdbcTemplate.execute("DELETE FROM film_likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");

        // Создаем тестовый фильм
        testFilm = FilmDto.builder()
                .name("Тестовый фильм")
                .description("Описание тестового фильма")
                .releaseDate(LocalDate.now().minusYears(1))
                .duration(120L)
                .mpa(new MpaDto(1, "G", "General Audiences"))
                .genres(new HashSet<>())
                .build();
    }

    @Test
    void deleteFilm_ShouldSuccessfullyDeleteFilm() {
        FilmDto createdFilm = filmController.addFilm(testFilm);
        filmController.deleteFilm(createdFilm.getId());

        // Проверяем через прямой запрос к БД
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM films WHERE id = ?",
                Integer.class,
                createdFilm.getId()
        );
        assertEquals(0, count);
    }

    @Test
    void deleteNonExistentFilm_ShouldThrowNotFoundException() {
        // Проверка
        assertThrows(NotFoundException.class, () -> filmController.deleteFilm(9999));
    }

    @Test
    void deleteFilm_ShouldRemoveRelatedGenres() {
        // Подготовка
        FilmDto createdFilm = filmController.addFilm(testFilm);

        // Добавляем жанр (имитируем привязку жанра к фильму)
        String addGenreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.update(addGenreSql, createdFilm.getId(), 1);

        // Действие
        filmController.deleteFilm(createdFilm.getId());

        // Проверка
        String checkGenresSql = "SELECT COUNT(*) FROM film_genres WHERE film_id = ?";
        int genresCount = jdbcTemplate.queryForObject(checkGenresSql, Integer.class, createdFilm.getId());
        assertEquals(0, genresCount, "Связи с жанрами должны быть удалены вместе с фильмом");
    }

    @Test
    void deleteFilm_AfterDeletionShouldNotAppearInGetAll() {
        // Подготовка
        FilmDto film1 = filmController.addFilm(testFilm);
        FilmDto film2 = filmController.addFilm(testFilm.toBuilder().name("Другой фильм").build());

        // Действие
        filmController.deleteFilm(film1.getId());

        // Проверка
        assertEquals(1, filmController.getAll().size(), "Должен остаться только один фильм");
    }
}