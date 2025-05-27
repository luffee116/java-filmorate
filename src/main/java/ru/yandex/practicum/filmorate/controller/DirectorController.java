package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService service;

    @GetMapping
    public List<Director> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable int id) {
        return service.get(id);
    }

    @PostMapping
    public Director create(@RequestBody Director director) {
        return service.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        return service.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.delete(id);
    }
}