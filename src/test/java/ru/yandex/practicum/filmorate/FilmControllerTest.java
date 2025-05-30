/*package ru.yandex.practicum.filmorate;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.repository.impl.*;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserFeedService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class})
public class FilmControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FilmController filmController;

    private FilmDto film;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("delete from films");

        UserDbStorage userDbStorage = new UserDbStorage(jdbcTemplate);
        FilmDbStorage filmDbStorage = new FilmDbStorage(jdbcTemplate);
        UserFeedDbStorage feedDbStorage = new UserFeedDbStorage(jdbcTemplate);
        UserFeedService feedService = new UserFeedService(feedDbStorage);
        FilmService filmService = new FilmService(filmDbStorage, userDbStorage, feedService);

        film = new FilmDto();
        film.setId(1);
        film.setName("Диктатор");
        film.setDescription("Фильм о лучшем диктаторе");
        film.setReleaseDate(LocalDate.of(2020,12,1));
        film.setDuration(100L);
        film.setMpa(new MpaDto(1, null, null));
        film.setGenres(new HashSet<>(1));
    }

    @Test
    void testCreateFilm() {
        filmController.addFilm(film);

        assertNotNull(film.getId());
        assertEquals("Диктатор", film.getName());
        assertEquals("Фильм о лучшем диктаторе", film.getDescription());
        assertEquals(LocalDate.of(2020,12,1), film.getReleaseDate());
        assertEquals(100L, film.getDuration());
    }

    @Test
    void testUpdateFilm() {
        FilmDto createdFilm = filmController.addFilm(film);

        FilmDto newFilm = new FilmDto();
        newFilm.setId(createdFilm.getId());
        newFilm.setName("Один дома");
        newFilm.setDescription(createdFilm.getDescription());
        newFilm.setReleaseDate(createdFilm.getReleaseDate());
        newFilm.setDuration(createdFilm.getDuration());
        newFilm.setMpa(new MpaDto(1, null, null));
        newFilm.setGenres(new HashSet<>(1));

        FilmDto updated = filmController.updateFilm(newFilm);

        assertEquals("Один дома", updated.getName());
        assertEquals("Фильм о лучшем диктаторе", updated.getDescription());
        assertEquals(LocalDate.of(2020,12,1), updated.getReleaseDate());
        assertEquals(100L, updated.getDuration());
    }

    @Test
    void getAllFilms() {
        filmController.addFilm(film);
        filmController.addFilm(film);

        Collection<FilmDto> films = filmController.getAll();
        assertEquals(2, films.size());
    }

    @Test
    void testEmptyName() {
        film.setName(" ");

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void testNullName() {
        film.setName(null);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void testOver200SymbolsDescription() {
        film.setDescription("Фильм рассказывает историю жестокого, но харизматичного диктатора, который правит вымышленной страной с помощью культа личности, пропаганды и абсурдных указов. Когда его режим оказывается под угрозой, он вынужден отправиться в США, где сталкивается с реалиями демократии, свободы слова и прав человека.\n" +
                "\n" +
                "Смешной и одновременно провокационный, фильм высмеивает авторитарные режимы, показывая, как власть развращает, а пропаганда искажает реальность. Главный герой – одновременно тиран и жертва собственной системы, чьи нелепые поступки заставляют задуматься о природе власти.\n" +
                "\n" +
                "Идеальный выбор для тех, кто любит черный юмор, политическую сатиру и нестандартные комедии. \"Диктатор\" – это не просто смешно, это смешно до боли!");

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void testNotCorrectReleasedDate() {
        film.setReleaseDate(LocalDate.of(1700,12,1));

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    void testNotCorrectDuration() {
        film.setDuration(-1L);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }
}

 */