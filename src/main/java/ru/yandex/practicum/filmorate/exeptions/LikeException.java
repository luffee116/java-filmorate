package ru.yandex.practicum.filmorate.exeptions;

public class LikeException extends RuntimeException {
    public LikeException(final String message) {
        super(message);
    }
}