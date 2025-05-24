package ru.yandex.practicum.filmorate.exeptions;

public class FilmUpdateException extends RuntimeException {
    public FilmUpdateException(String message) {
        super(message);
    }
}