package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {
    private final DirectorStorage storage;

    @Override
    public Director create(Director director) {
        return storage.create(director);
    }

    @Override
    public Director update(Director director) {
        if (!storage.existsById(director.getId())) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        return storage.update(director);
    }

    @Override
    public void delete(int id) {
        storage.delete(id);
    }

    @Override
    public Director get(int id) {
        return storage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден."));
    }

    @Override
    public List<Director> getAll() {
        return storage.getAll();
    }
}