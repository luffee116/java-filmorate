package ru.yandex.practicum.filmorate.mapper.toEntity;

import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

public class DirectorMapper {
    public static Director toDirector(DirectorDto directorDto) {
        Director director = new Director();

        director.setId(Math.toIntExact(directorDto.getId()));
        director.setName(directorDto.getName());

        return director;
    }
}
