package ru.yandex.practicum.filmorate.exeptions;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}