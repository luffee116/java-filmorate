package ru.yandex.practicum.filmorate.exeptions;

/**
 * Исключение для ситуаций, когда запрашиваемый объект не найден.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}