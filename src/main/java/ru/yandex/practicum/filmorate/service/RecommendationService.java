package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.UserDbStorage;

import java.util.*;

/**
 * Сервис для получения рекомендаций фильмов пользователю на основе
 * лайков других пользователей с похожими вкусами.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final FilmService filmService;
    private final UserDbStorage userStorage;

    /**
     * Возвращает список рекомендованных фильмов для пользователя.
     * Рекомендации формируются на основе пользователя, у которого
     * максимальное пересечение лайков с текущим пользователем.
     * Из рекомендаций исключаются фильмы, которые уже лайкнул текущий пользователь.
     *
     * @param userId идентификатор пользователя, для которого нужны рекомендации
     * @return список рекомендованных FilmDto; пустой список, если похожих пользователей нет
     */
    public List<FilmDto> getRecommendations(Integer userId) {
        Set<Integer> likedByUser = filmService.getLikedFilmsIds(userId);

        Integer mostSimilarUserId = findMostSimilarUser(userId, likedByUser);
        if (mostSimilarUserId == null) {
            return Collections.emptyList();
        }

        return filmService.getLikedFilmsIds(mostSimilarUserId).stream()
                .filter(filmId -> !likedByUser.contains(filmId))
                .map(filmService::getFilmById)
                .toList();
    }

    /**
     * Находит идентификатор пользователя, у которого максимальное пересечение
     * лайкнутых фильмов с заданным пользователем.
     *
     * @param userId идентификатор пользователя, для которого ищем похожего
     * @param likedByUser множество идентификаторов фильмов, лайкнутых пользователем
     * @return идентификатор самого похожего пользователя, или null, если таких нет
     */
    private Integer findMostSimilarUser(Integer userId, Set<Integer> likedByUser) {
        return userStorage.getAll().stream()
                .map(User::getId)
                .filter(id -> !id.equals(userId))
                .max(Comparator.comparingInt(otherId ->
                        intersectionSize(likedByUser, filmService.getLikedFilmsIds(otherId))
                ))
                .orElse(null);
    }

    /**
     * Вычисляет размер пересечения двух множеств идентификаторов фильмов.
     *
     * @param firstSet первое множество идентификаторов
     * @param secondSet второе множество идентификаторов
     * @return количество общих элементов в двух множествах
     */
    private int intersectionSize(Set<Integer> firstSet, Set<Integer> secondSet) {
        Set<Integer> copy = new HashSet<>(firstSet);
        copy.retainAll(secondSet);
        return copy.size();
    }
}
