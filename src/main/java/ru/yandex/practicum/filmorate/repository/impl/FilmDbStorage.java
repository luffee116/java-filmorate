package ru.yandex.practicum.filmorate.repository.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.dto.FilmDtoMapper;
import ru.yandex.practicum.filmorate.mapper.toEntity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.GenreStorage;
import ru.yandex.practicum.filmorate.repository.TypeEntity;
import ru.yandex.practicum.filmorate.rowMappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.rowMappers.GenreDtoRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;


@Repository
@Slf4j
public class FilmDbStorage extends BaseDbStorage implements FilmStorage {

    private GenreStorage genreStorage;
    private DirectorStorage directorStorage;
    private JdbcTemplate jdbcTemplate;
    private FilmDtoMapper filmDtoMapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         GenreStorage genreStorage,
                         DirectorStorage directorStorage,
                         FilmDtoMapper filmDtoMapper) {
        super(jdbcTemplate);
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.filmDtoMapper = filmDtoMapper;
    }

    // SQL queries
    private static final String CREATE_FILM_QUERY = """
            INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
            VALUES (?, ?, ?, ?, ?);
            """;

    private static final String UPDATE_FILM_QUERY = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
            WHERE id = ?;
            """;

    private static final String GET_FILM_BY_ID_QUERY = """
            SELECT f.*, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description
            FROM films f
            JOIN mpa_rating m ON f.mpa_rating_id = m.id
            WHERE f.id = ?;
            """;

    private static final String GET_ALL_FILMS_QUERY = """
            SELECT f.*, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description
            FROM films f
            LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
            """;

    private static final String GET_FILMS_BY_DIRECTOR_SORTED = """
            SELECT f.*, m.name AS mpa_name, m.description AS mpa_description,
                   d.id AS director_id, d.name AS director_name
            FROM films f
            JOIN film_director fd ON f.id = fd.film_id
            JOIN director d ON fd.director_id = d.id
            LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
            WHERE d.id = ?
            """;

    private static final String GET_FILMS_BY_DIRECTOR_SORTED_LIKES = """
            LEFT JOIN (
                SELECT film_id, COUNT(user_id) AS likes_count
                FROM film_likes
                GROUP BY film_id
            ) fl ON f.id = fl.film_id
            ORDER BY COALESCE(fl.likes_count, 0) DESC
            """;
    private static final String ADD_LIKE_TO_FILM_QUERY = """
            INSERT INTO film_likes (USER_ID, FILM_ID) VALUES (?, ?);
            """;
    private static final String DELETE_LIKE_TO_FILM_QUERY = """
            DELETE FROM film_likes WHERE USER_ID = ? AND FILM_ID = ?;
            """;
    private static final String GET_POPULAR_FILM_QUERY = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.DURATION,
                   m.id mpa_id,
                   m.name mpa_name,
                   m.description mpa_description,
                    COUNT(l.USER_ID) likes_count
                FROM films f
                LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
                LEFT JOIN film_likes l ON f.id = l.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?;
            """;
    private static final String ADD_FILM_GENRES_QUERY = """
            INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);
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

    private static final String DELETE_FILM_GENRES_BY_ID = """
            DELETE FROM film_genres WHERE film_id = ?;
            """;
    private static final String GET_COMMON_FILMS = """
            SELECT f.*, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description
            FROM films f
            JOIN film_likes fl1 ON f.id = fl1.film_id
            JOIN film_likes fl2 ON f.id = fl2.film_id
            JOIN mpa_rating m ON f.mpa_rating_id = m.id
            WHERE fl1.user_id = ? AND fl2.user_id = ?
            GROUP BY f.id, m.id, m.name, m.description
            ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC
            """;
    private static final String GET_LIKED_FILMS_BY_USER_ID_QUERY = """
            SELECT film_id
            FROM film_likes
            WHERE user_id = ?;
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
            PreparedStatement ps = connection.prepareStatement(CREATE_FILM_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration().intValue());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        int filmId = keyHolder.getKey().intValue();
        film.setId(filmId);

        // Сохраняем жанры и режиссеров
        genreStorage.setGenresForFilm(film);
        directorStorage.setDirectorsForFilm(film);

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

        // Обновляем жанры и режиссеров
        genreStorage.clearGenresForFilm(film.getId());
        genreStorage.setGenresForFilm(film);

        directorStorage.clearDirectorsForFilm(film.getId());
        directorStorage.setDirectorsForFilm(film);

        log.info("Film updated: {}", film.getId());
        return film;
    }

    // Получение всех фильмов ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public List<Film> getAll() {
        Map<Integer, List<Integer>> likes = setUpLikes();
        Map<Integer, List<GenreDto>> genres = setUpGenres();
        Map<Integer, List<Director>> directors = setUpDirectors();

        List<FilmDto> films = jdbcTemplate.query(
                GET_ALL_FILMS_QUERY,
                (rs, rowNum) -> filmDtoMapper.mapToFilmDto(rs)
        );

        List<FilmDto> filmsToResponse = addGenresAndLikesToFilmList(films, likes, genres);
        filmsToResponse = addDirectorsToFilmList(filmsToResponse, directors);

        return filmsToResponse.stream()
                .map(FilmMapper::mapToFilm)
                .toList();
    }

    // Получение фильма по id ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<Film> getById(Integer id) {
        try {
            FilmDto filmDto = jdbcTemplate.queryForObject(
                    GET_FILM_BY_ID_QUERY,
                    (rs, rowNum) -> filmDtoMapper.mapToFilmDto(rs),
                    id
            );

            if (filmDto != null) {
                addGenresAndLikesToFilm(filmDto);
                addDirectorsToFilm(filmDto);
                return Optional.of(FilmMapper.mapToFilm(filmDto));
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
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
        Map<Integer, List<Integer>> likes = setUpLikes();
        Map<Integer, List<GenreDto>> genres = setUpGenres();

        List<FilmDto> films = jdbcTemplate.query(GET_POPULAR_FILM_QUERY, new FilmRowMapper(), count);

        List<FilmDto> filmsToResponse = addGenresAndLikesToFilmList(films, likes, genres);
        return filmsToResponse.stream().map(FilmMapper::mapToFilm).toList();
    }

    /**
     * Удаляет фильм по идентификатору.
     *
     * @param id идентификатор фильма
     * @throws NotFoundException если фильм не найден
     */
    @Override
    @Transactional
    public void delete(Integer id) {
        // Проверяем существование фильма
        if (!existsById(id)) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        // Удаляем связанные данные
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", id);
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", id);

        // Удаляем фильм
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    /**
     * Проверяет существование фильма в базе данных по указанному идентификатору.
     * <p>
     * Метод выполняет оптимизированный запрос к базе данных, используя оператор EXISTS,
     * который прекращает поиск после нахождения первой записи.
     * </p>
     *
     * @param id идентификатор фильма для проверки (должен быть не null)
     * @return true - если фильм с указанным ID существует, false - если не существует
     */
    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM films WHERE id = ?)";
        try {
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
        } catch (DataAccessException e) {
            log.error("Проверка ошибки существование пленки с идентификатором: {}", id, e);
            return false;
        }
    }

    /**
     * Получает список фильмов, которые понравились как указанному пользователю, так и его другу.
     * Использует SQL-запрос для извлечения общих фильмов, затем обогащает их жанрами и лайками.
     *
     * @param userId   идентификатор пользователя.
     * @param friendId идентификатор друга пользователя.
     * @return список фильмов, понравившихся обоим пользователям.
     */
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        Map<Integer, List<Integer>> likes = setUpLikes();
        Map<Integer, List<GenreDto>> genres = setUpGenres();

        List<FilmDto> films = jdbcTemplate.query(GET_COMMON_FILMS, new FilmRowMapper(), userId, friendId);
        List<FilmDto> filmsToResponse = addGenresAndLikesToFilmList(films, likes, genres);

        return filmsToResponse.stream().map(FilmMapper::mapToFilm).toList();
    }

    public Set<Integer> getLikedFilmsIds(Integer userId) {
        return new HashSet<>(jdbcTemplate.queryForList(GET_LIKED_FILMS_BY_USER_ID_QUERY, Integer.class, userId));
    }

    @Override
    public List<Film> getFilmsByDirectorSorted(int directorId, String sortBy) {
        String sqlBase = """
            SELECT f.*,
                   m.name AS mpa_name,
                   m.description AS mpa_description,
                   d.id AS director_id,
                   d.name AS director_name
            FROM films f
            JOIN film_director fd ON f.id = fd.film_id
            JOIN director d ON fd.director_id = d.id
            LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
            """;

        String sql;

        if ("year".equals(sortBy)) {
            sql = sqlBase + """
                LEFT JOIN (
                    SELECT film_id, COUNT(user_id) AS likes_count
                    FROM film_likes
                    GROUP BY film_id
                ) fl ON f.id = fl.film_id
                WHERE d.id = ?
                ORDER BY f.release_date
                """;
        } else if ("likes".equals(sortBy)) {
            sql = sqlBase + """
                LEFT JOIN (
                    SELECT film_id, COUNT(user_id) AS likes_count
                    FROM film_likes
                    GROUP BY film_id
                ) fl ON f.id = fl.film_id
                WHERE d.id = ?
                ORDER BY COALESCE(fl.likes_count, 0) DESC
                """;
        } else {
            sql = sqlBase + "WHERE d.id = ?";
        }

        List<FilmDto> filmDtos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> filmDtoMapper.mapToFilmDto(rs),
                directorId
        );

        return filmDtos.stream()
                .peek(this::addGenresAndLikesToFilm)
                .peek(this::addDirectorsToFilm)
                .map(FilmMapper::mapToFilm)
                .toList();
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
        filmDto.setGenres(new HashSet<>(getGenresForFilm(filmDto.getId())));
        filmDto.setLikes(new HashSet<>(getLikesForFilm(filmDto.getId())));
    }

    private void addDirectorsToFilm(FilmDto filmDto) {
        List<Director> directors = jdbcTemplate.query(
                "SELECT d.* FROM director d JOIN film_director fd ON d.id = fd.director_id WHERE fd.film_id = ?",
                (rs, rowNum) -> new Director(
                        rs.getInt("id"),
                        rs.getString("name")
                ),
                filmDto.getId()
        );
        filmDto.setDirectors(new HashSet<>(directors));
    }

    private List<FilmDto> addDirectorsToFilmList(List<FilmDto> films, Map<Integer, List<Director>> directorsMap) {
        films.forEach(f -> {
            f.setDirectors(new HashSet<>(directorsMap.getOrDefault(f.getId(), List.of())));
        });
        return films;
    }

    private List<GenreDto> getGenresForFilm(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.* FROM genres g JOIN film_genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?",
                new GenreDtoRowMapper(),
                filmId
        );
    }

    private List<Integer> getDirectorsForFilm(int filmId) {
        return jdbcTemplate.queryForList(
                "SELECT director_id FROM film_director WHERE film_id = ?",
                Integer.class,
                filmId
        );
    }

    private List<Integer> getLikesForFilm(int filmId) {
        return jdbcTemplate.queryForList(
                "SELECT user_id FROM film_likes WHERE film_id = ?",
                Integer.class,
                filmId
        );
    }

    //Наполнение фильма
    private List<FilmDto> addGenresAndLikesToFilmList(List<FilmDto> films,
                                                      Map<Integer, List<Integer>> likes,
                                                      Map<Integer, List<GenreDto>> genres) {
        films.forEach(f -> {
            f.setGenres(new HashSet<>(genres.getOrDefault(f.getId(), List.of())));
            f.setLikes(new HashSet<>(likes.getOrDefault(f.getId(), List.of())));
        });
        return films;
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
        return jdbcTemplate.query(
                "SELECT fg.film_id, g.* FROM film_genres fg JOIN genres g ON fg.genre_id = g.genre_id",
                rs -> {
                    Map<Integer, List<GenreDto>> map = new HashMap<>();
                    while (rs.next()) {
                        map.computeIfAbsent(rs.getInt("film_id"), k -> new ArrayList<>())
                                .add(new GenreDto(rs.getInt("genre_id"), rs.getString("name")));
                    }
                    return map;
                }
        );
    }

    private Map<Integer, List<Integer>> setUpLikes() {
        return jdbcTemplate.query(
                "SELECT film_id, user_id FROM film_likes",
                rs -> {
                    Map<Integer, List<Integer>> map = new HashMap<>();
                    while (rs.next()) {
                        map.computeIfAbsent(rs.getInt("film_id"), k -> new ArrayList<>())
                                .add(rs.getInt("user_id"));
                    }
                    return map;
                }
        );
    }

    /**
     * Добавление нового фильма с сохранением жанров и режиссёров.
     */
    @Override
    public Film createFilm(Film film) {
        String sql = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        int filmId = keyHolder.getKey().intValue();
        film.setId(filmId);

        genreStorage.setGenresForFilm(film);
        directorStorage.setDirectorsForFilm(film);

        return film;
    }

    /**
     * Обновление фильма с очисткой и повторной установкой жанров и режиссёров.
     */
    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        genreStorage.clearGenresForFilm(film.getId());
        genreStorage.setGenresForFilm(film);

        directorStorage.clearDirectorsForFilm(film.getId());
        directorStorage.setDirectorsForFilm(film);

        return film;
    }

    private Map<Integer, List<Director>> setUpDirectors() {
        return jdbcTemplate.query(
                "SELECT fd.film_id, d.* FROM film_director fd JOIN director d ON fd.director_id = d.id",
                rs -> {
                    Map<Integer, List<Director>> map = new HashMap<>();
                    while (rs.next()) {
                        Director director = new Director(
                                rs.getInt("id"),
                                rs.getString("name")
                        );
                        map.computeIfAbsent(rs.getInt("film_id"), k -> new ArrayList<>())
                                .add(director);
                    }
                    return map;
                }
        );
    }

}