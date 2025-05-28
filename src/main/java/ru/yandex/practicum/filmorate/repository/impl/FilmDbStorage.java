package ru.yandex.practicum.filmorate.repository.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.toEntity.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.TypeEntity;
import ru.yandex.practicum.filmorate.rowMappers.DirectorDtoRowMapper;
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
    private static final String INSERT_FILM_DIRECTOR_QUERY = """
            INSERT INTO film_directors (film_id, director_id) VALUES (?, ?);
            """;
    private static final String DELETE_LIKE_TO_FILM_QUERY = """
            DELETE FROM film_likes WHERE USER_ID = ? AND FILM_ID = ?;
            """;
    private static final String GET_POPULAR_FILM_QUERY = """
                SELECT f.*,
                   mr.id mpa_id,
                   mr.name mpa_name,
                   mr.description mpa_description,
                   fl.likes_count
            FROM films f
            JOIN review_ratings mr ON f.mpa_rating_id = mr.id
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS likes_count
                FROM film_likes
                GROUP BY film_id
            ) fl ON f.id = fl.film_id
            ORDER BY likes_count DESC,
                     f.id DESC
            LIMIT ?;
            """;
    private static final String FIND_FILMS_DIRECTORS_FOR_LIKES = """
            SELECT f.id AS id,
                   f.name AS name,
                   f.description AS description,
                   f.release_date,
                   f.duration,
                   mr.id AS mpa_id,
                   mr.name AS mpa_name,
                   mr.description AS mpa_description,
                   COALESCE(fl.likes_count, 0) AS likes_count
            FROM films f
            JOIN mpa_rating mr ON f.mpa_rating_id = mr.id
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS likes_count
                FROM film_likes
                GROUP BY film_id
            ) fl ON f.id = fl.film_id
            WHERE f.id IN (
                SELECT film_id
                FROM film_directors
                WHERE director_id = ?
            )
            ORDER BY likes_count DESC;
            """;

    private static final String ADD_FILM_GENRES_QUERY = """
            INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);
            """;
    private static final String GET_ALL_FILMS_QUERY = """
               SELECT f.*,
                   m.id mpa_id,
                   m.name mpa_name,
                   m.description mpa_description
            FROM films f
            LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id;
            """;
    private static final String GET_GENRES_ID_FOR_FILM_ID_QUERY = """
                    SELECT g.genre_id,
                           g.name
                    FROM genres g
                    JOIN film_genres fg ON g.GENRE_ID = fg.GENRE_ID
                    WHERE fg.FILM_ID = ?
                    ORDER BY g.genre_id ASC;
            """;
    private static final String FIND_FILMS_DIRECTORS_FOR_DATES = """
            SELECT f.id AS id,
                   f.name AS name,
                   f.description AS description,
                   f.release_date,
                   f.duration,
                   mr.id AS mpa_id,
                   mr.name AS mpa_name,
                   mr.description AS mpa_description,
                   COALESCE(fl.likes_count, 0) AS likes_count
            FROM films f
            JOIN mpa_rating mr ON f.mpa_rating_id = mr.id
            LEFT JOIN (
                SELECT film_id, COUNT(*) AS likes_count
                FROM film_likes
                GROUP BY film_id
            ) fl ON f.id = fl.film_id
            WHERE f.id IN (
                SELECT film_id
                FROM film_directors
                WHERE director_id = ?
            )
            ORDER BY f.release_date ASC;
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
    private static final String DELETE_FILM_DIRECTORS_BY_ID = """
            DELETE FROM film_directors WHERE film_id = ?;
            """;

    private static final String FIND_DIRECTORS_ID_BY_FILM_ID_QUERY = """
            SELECT d.id,
                   d.name
            FROM directors d
            JOIN film_directors fd
            ON d.id = fd.director_id
            WHERE fd.film_id = ?;
            """;

    private static final String GET_POPULAR_FILMS_BY_GENRE_AND_YEAR = """
            SELECT f.*, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description
            FROM films f
            JOIN mpa_rating m ON f.mpa_rating_id = m.id
            LEFT JOIN film_genres fg ON f.id = fg.film_id
            WHERE (fg.genre_id = ? OR ?) AND (EXTRACT(YEAR FROM f.release_date) = ? OR ?)
            ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC
            LIMIT ?
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
        // обновить режиссеров фильма
        jdbcTemplate.update(DELETE_FILM_DIRECTORS_BY_ID, film.getId());
        saveFilmDirectors(film.getId().longValue(), film.getDirectors());

        log.info("Film updated: {}", film.getId());
        return film;
    }

    // Получение всех фильмов ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public List<Film> getAll() {

        List<FilmDto> films = jdbcTemplate.query(GET_ALL_FILMS_QUERY, new FilmRowMapper());
        films.forEach(this::addGenresAndLikesToFilm);

        return films.stream().map(FilmMapper::mapToFilm).toList();
    }

    // Получение фильма по id ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @Override
    public Optional<Film> getById(Integer id) {
        try {
            // 1. Получаем основную информацию о фильме
            FilmDto filmDto = jdbcTemplate.queryForObject(
                    GET_FILM_BY_ID_QUERY,
                    new FilmRowMapper(),
                    id
            );

            // 2. Если фильм найден, дополняем его данными
            if (filmDto != null) {
                addGenresAndLikesToFilm(filmDto);
                return Optional.of(FilmMapper.mapToFilm(filmDto));
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            // 3. Обрабатываем случай, когда фильм не найден
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
        List<FilmDto> popularFilms = jdbcTemplate.query(GET_POPULAR_FILM_QUERY, new FilmRowMapper(), count);

        // загрузить жанры, режиссеров и лайки для выбранных фильмов
        popularFilms.forEach(this::addGenresAndLikesToFilm);

        return popularFilms.stream()
                .map(FilmMapper::mapToFilm)
                .toList();
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

    @Override
    public List<Film> getPopularFilmsByGenreAndYear(int count, Integer genreId, Integer year) {
        Map<Integer, List<Integer>> likes = setUpLikes();
        Map<Integer, List<GenreDto>> genres = setUpGenres();

        String sql = GET_POPULAR_FILMS_BY_GENRE_AND_YEAR;

        List<FilmDto> films;
        try {
            films = jdbcTemplate.query(sql, new FilmRowMapper(), genreId, genreId == null, year, year == null, count);
        } catch (DataAccessException e) {
            System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
            return Collections.emptyList();
        }

        List<FilmDto> filmsToResponse = addGenresAndLikesToFilmList(films, likes, genres);

        return filmsToResponse.stream().map(FilmMapper::mapToFilm).toList();
    }

    public Set<Integer> getLikedFilmsIds(Integer userId) {
        return new HashSet<>(jdbcTemplate.queryForList(GET_LIKED_FILMS_BY_USER_ID_QUERY, Integer.class, userId));
    }

    //--- Получение фильмов режиссера, отсортированных по годам или лайкам ---------------------------------------------
    public Collection<Film> getFilmsDirector(Long directorId, String sortBy) {
        List<FilmDto> films = new ArrayList<>();
        if (sortBy.equals("likes")) {
            films = jdbcTemplate.query(FIND_FILMS_DIRECTORS_FOR_LIKES, new FilmRowMapper(), directorId);
        } else if (sortBy.equals("year")) {
            films = jdbcTemplate.query(FIND_FILMS_DIRECTORS_FOR_DATES, new FilmRowMapper(), directorId);
        } else {
            throw new IllegalArgumentException("sortBy must be 'likes' or 'year'");
        }

        // загрузить жанры, режиссеров и лайки для всех фильмов
        films.forEach(this::addGenresAndLikesToFilm);

        return films.stream()
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
        Integer filmId = filmDto.getId();
        filmDto.setGenres(loadGenresForFilm(filmId));
        filmDto.setDirectors(loadDirectorsForFilm(Long.valueOf(filmId)));
        filmDto.setLikes(loadLikesForFilm(filmId));
    }

    //Наполнение фильма
    private List<FilmDto> addGenresAndLikesToFilmList(List<FilmDto> films,
                                                      Map<Integer, List<Integer>> likes,
                                                      Map<Integer, List<GenreDto>> genres) {
        films.forEach(filmDto -> {
            filmDto.setGenres(new HashSet<>(genres.getOrDefault(filmDto.getId(), List.of())));
            filmDto.setLikes(new HashSet<>(likes.getOrDefault(filmDto.getId(), List.of())));
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

    private void checkGenreExists(Genre genre) {
        checkEntityExists(genre.getId(), TypeEntity.GENRE);
    }

    private void checkDirectorExists(Director director) {
        checkEntityExists(director.getId(), TypeEntity.DIRECTOR);
    }

    private Set<DirectorDto> loadDirectorsForFilm(Long filmId) {
        return new HashSet<>(jdbcTemplate.query(FIND_DIRECTORS_ID_BY_FILM_ID_QUERY, new DirectorDtoRowMapper(), filmId));
    }

    private void saveFilmDirectors(Long filmId, Set<Director> directors) {
        if (directors != null && !directors.isEmpty()) {
            directors.forEach(this::checkDirectorExists);
            directors.forEach(director -> jdbcTemplate.update(INSERT_FILM_DIRECTOR_QUERY, filmId, director.getId()));
        }
    }

}