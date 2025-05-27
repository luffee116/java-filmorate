package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Director create(Director director);

    Director update(Director director);

    void delete(int id);

    Optional<Director> getById(int id);

    List<Director> getAll();

    void setDirectorsForFilm(Film film); // Добавить режиссёров к фильму

    void clearDirectorsForFilm(int filmId); // Очистить режиссёров фильма
}