package ru.yandex.practicum.filmorate.exeptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record Exception(
        String error,
        @JsonFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
        LocalDateTime timeStamp
) {
}