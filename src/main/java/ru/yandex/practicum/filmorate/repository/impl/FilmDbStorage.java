package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.mapper.toEntity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.TypeEntity;
import ru.yandex.practicum.filmorate.rowMappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.rowMappers.GenreDtoRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;

import java.util.*;

@Repository
@Slf4j
public class FilmDbStorage extends BaseDbStorage implements FilmStorage {
    private static final String CREATE_FILM_QUERY = """
            INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?);
            """;
    private static final String UPDATE_FILM_QUERY = """
            UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
            WHERE id = ?;
            """;
    private static final String ADD_LIKE_TO_FILM_QUERY = """
            INSERT INTO film_likes (USER_ID, FILM_ID) VALUES (?, ?);
            """;
    private static final String DELETE_LIKE_TO_FILM_QUERY = """
            DELETE FROM film_likes WHERE USER_ID = ? AND FILM_ID = ?;
            """;
    private static final String GET_POPULAR_FILM_QUERY = """
            SELECT
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.ID mpa_id,
                    m.name mpa_name,
                    m.description mpa_description,
                    COUNT(fl.user_id) likes_count
                FROM
                    films f
                JOIN
                    film_likes fl ON f.id = fl.film_id
                JOIN
                    mpa_rating m ON f.mpa_rating_id = m.id
                GROUP BY
                    f.id, f.name, f.description, f.release_date, f.duration, m.name
                ORDER BY
                    likes_count DESC
                LIMIT ?;
            """;
    private static final String ADD_FILM_GENRES_QUERY = """
            INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);
            """;
    private static final String GET_ALL_FILMS_QUERY = """
                    SELECT f.*,
                           m.ID mpa_id,
                           m.name mpa_name,
                           m.description mpa_description
                    FROM films f
                    JOIN mpa_rating m ON f.mpa_rating_id = m.id;
            """;
    private static final String GET_GENRES_ID_FOR_FILM_ID_QUERY = """
                    SELECT g.genre_id,
                           g.name
                    FROM genres g
                    JOIN film_genres fg ON g.GENRE_ID = fg.GENRE_ID
                    WHERE fg.FILM_ID = ?;
            """;
    private static final String GET_LIKES_BY_FILM_ID_QUERY = """
                    SELECT user_id
                    FROM film_likes
                    WHERE film_id = ?;
            """;
    private static final String GET_FILM_BY_ID_QUERY = """
                            SELECT f.*,
                                   m.ID mpa_id,
                                   m.NAME mpa_name,
                                   m.DESCRIPTION mpa_description
                            FROM films f
                            JOIN mpa_rating m ON f.mpa_rating_id = m.id
                            WHERE f.id = ?;
            """;
    private static final String DELETE_FILM_GENRES_BY_ID = """
            DELETE FROM film_genres WHERE film_id = ?;
            """;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // Создание фильма –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Film create(Film film) {
        checkMpaRating(film);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_FILM_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration().intValue());
            ps.setInt(5, film.getMpa().getId() != null ? film.getMpa().getId() : 0);

            return ps;
        }, keyHolder);

        int filmId = keyHolder.getKey().intValue();
        film.setId(filmId);

        // Сохранение жанров фильма в БД
        addFilmGenres(film.getGenres(), filmId);
        log.info("Film created: {}", filmId);

        return film;
    }

    // Обновление фильма –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Film update(Film film) {
        checkFilm(film);
        checkMpaRating(film);

        jdbcTemplate.update(
                UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        // Обновление жанров фильма
        jdbcTemplate.update(DELETE_FILM_GENRES_BY_ID, film.getId());
        addFilmGenres(film.getGenres(), film.getId());
        log.info("Film updated: {}", film.getId());
        return film;
    }

    // Получение всех фильмов ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public List<Film> getAll() {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date,f.DURATION, m.id mpa_id, m.name mpa_name, m.description mpa_description
                FROM films f
                LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
                """;
        List<FilmDto> films = jdbcTemplate.query(sql, new FilmRowMapper());

        films.forEach(film -> film.setGenres(new HashSet<>(setUpGenres().getOrDefault(film.getId(), List.of()))));
        films.forEach(film -> film.setLikes(new HashSet<>(setUpLikes().getOrDefault(film.getId(), List.of()))));
        return films.stream().map(FilmMapper::mapToFilm).toList();
    }

    // Получение фильма по id ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<Film> getById(Integer id) {
        FilmDto filmDto = jdbcTemplate.queryForObject(GET_FILM_BY_ID_QUERY, new FilmRowMapper(), id);

        if (filmDto != null) {
            addGenresAndLikesToFilm(filmDto);
            return Optional.of(FilmMapper.mapToFilm(filmDto));
        }
        return Optional.empty();
    }

    // Добавление лайка фильму –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<Boolean> addLike(Integer filmId, Integer userId) {
        int rowsUpdated = jdbcTemplate.update(ADD_LIKE_TO_FILM_QUERY, userId, filmId);
        return Optional.of(rowsUpdated > 0);
    }

    // Удаление лайка фильму –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<Boolean> removeLike(Integer filmId, Integer userId) {
        int rowsDeleted = jdbcTemplate.update(DELETE_LIKE_TO_FILM_QUERY, userId, filmId);
        return Optional.of(rowsDeleted > 0);
    }

    // Получение списка популярных фильмов –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public List<Film> getPopularFilm(Integer count) {
        String sqlPopular = """
                SELECT f.id, f.name, f.description, f.release_date,f.DURATION, m.id mpa_id, m.name mpa_name, m.description mpa_description,
                       COUNT(l.USER_ID) likes_count
                FROM films f
                LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
                LEFT JOIN film_likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?;
                """;
        List<FilmDto> films = jdbcTemplate.query(sqlPopular, new FilmRowMapper(), count);

        films.forEach(film -> film.setGenres(new HashSet<>(setUpGenres().getOrDefault(film.getId(), List.of()))));
        films.forEach(film -> film.setLikes(new HashSet<>(setUpLikes().getOrDefault(film.getId(), List.of()))));
        return films.stream().map(FilmMapper::mapToFilm).toList();
    }


    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    // Добавление жанров
    private void addFilmGenres(Set<Genre> genresSet, int id) {
        if (genresSet != null && !genresSet.isEmpty()) {
            genresSet.forEach(this::checkGenre);
            genresSet.forEach(genre -> jdbcTemplate.update(ADD_FILM_GENRES_QUERY, id, genre.getId()));
        }
    }

    // Получение жанров фильма –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    private Set<GenreDto> loadGenresForFilm(Integer id) {
        return new HashSet<>(jdbcTemplate.query(GET_GENRES_ID_FOR_FILM_ID_QUERY, new GenreDtoRowMapper(), id));
    }

    // Получение лайков фильма –––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    private Set<Integer> loadLikesForFilm(Integer id) {
        return new HashSet<>(jdbcTemplate.queryForList(GET_LIKES_BY_FILM_ID_QUERY, Integer.class, id));
    }

    // Преобразование ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    private void addGenresAndLikesToFilm(FilmDto filmDto) {
        Integer filmId = filmDto.getId();
        filmDto.setGenres(loadGenresForFilm(filmId));
        filmDto.setLikes(loadLikesForFilm(filmId));
    }

    // Проверка существования фильма
    private void checkFilm(Film film) {
        checkEntityExist(film.getId(), TypeEntity.FILM);
    }

    // Проверка существования жанра
    private void checkGenre(Genre genre) {
        checkEntityExist(genre.getId(), TypeEntity.GENRE);
    }

    // Проверка существования рейтинга
    private void checkMpaRating(Film film) {
        checkEntityExist(film.getMpa().getId(), TypeEntity.RATING);
    }

    private Map<Integer, List<GenreDto>> setUpGenres() {
        String sql = """
                SELECT fg.film_id, g.*
                FROM film_genres fg
                JOIN genres g ON fg.GENRE_ID = g.genre_id
                """;

        Map<Integer, List<GenreDto>> filmGenres = jdbcTemplate.query(sql, rs -> {
            Map<Integer, List<GenreDto>> genres = new HashMap<>();
            while (rs.next()) {
                Integer filmId = rs.getInt("film_id");
                genres.computeIfAbsent(filmId, k -> new ArrayList<>())
                        .add(GenreDto.builder()
                                .id(rs.getInt("genre_id"))
                                .name(rs.getString("name"))
                                .build());
            }
            return genres;
        });
        return filmGenres;
    }

    private Map<Integer, List<Integer>> setUpLikes() {
        String sqlLikes = """
                SELECT f.id, fl.user_id
                FROM film_likes fl
                JOIN films f ON fl.film_id = f.id
                """;

        Map<Integer, List<Integer>> filmLikes = jdbcTemplate.query(sqlLikes, rs -> {
            Map<Integer, List<Integer>> likes = new HashMap<>();
            while (rs.next()) {
                Integer filmId = rs.getInt("id");
                likes.computeIfAbsent(filmId, k -> new ArrayList<>())
                        .add(rs.getInt("user_id"));
            }
            return likes;
        });
        return filmLikes;
    }
}