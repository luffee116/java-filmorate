package ru.yandex.practicum.filmorate.exeptions;

public class ReviewException extends RuntimeException {
    public ReviewException(String message) {
        super(message);
    }
}
