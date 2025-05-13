package ru.yandex.practicum.filmorate.exeptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Exception handleValidateException(ValidationException e) {
        return new Exception(
                e.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler({FilmNotFoundException.class, UserNotFoundException.class, NotFoundException.class, LikeException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Exception handleNotFound(final RuntimeException e) {
        return new Exception(
                e.getMessage(),
                LocalDateTime.now()
                );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Exception handleValidation(ConstraintViolationException e) {
        return new Exception("Validation exception", LocalDateTime.now());
    }


}
