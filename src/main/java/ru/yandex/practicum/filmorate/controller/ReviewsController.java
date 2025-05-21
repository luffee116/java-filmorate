package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewsController {
    ReviewService reviewService;

    /**
     * Создание отзыва
     *
     * @param reviewDto данные отзыва
     */
    @PostMapping
    public ReviewDto create(@RequestBody ReviewDto reviewDto) {
        return reviewService.createReview(reviewDto);
    }

    /**
     * Обновление отзыва
     *
     * @param reviewDto данные отзыва
     */
    @PutMapping
    public ReviewDto updateReview(@RequestBody ReviewDto reviewDto) {
        return reviewService.updateReview(reviewDto);
    }

    /**
     * Удаление отзыва
     *
     * @param id id отзыва в базе данных
     */
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable("id") String id) {
        if (id.matches("\\d+")) {
            reviewService.deleteReview(Integer.parseInt(id));
        } else {
            int lastId = reviewService.getLastReviewId();
            reviewService.deleteReview(lastId);
        }
    }

    /**
     * Получение отзыва
     *
     * @param id id отзыва в базе данных
     */
    @GetMapping("/{id}")
    public ReviewDto getReview(@PathVariable("id") Integer id) {
        return reviewService.getReviewById(id).orElseThrow(() -> new NotFoundException("Not found review"));
    }

    /**
     * Получение списка отзывов
     *
     * @param filmId id фильма в базе данных (Не обязательный параметр)
     * @param count  количество выводимых значений (По умолчанию 10)
     */
    @GetMapping
    public List<ReviewDto> getReviews(@RequestParam(required = false) Integer filmId,
                                      @RequestParam(required = false, defaultValue = "10") Integer count) {
        return reviewService.getReviews(filmId, count);
    }

    /**
     * Получение списка отзывов (Обрабатывает явный запрос /null)
     *
     * @param count количество выводимых значений (По умолчанию 10)
     */
    @GetMapping("/null")
    public List<ReviewDto> getReviewsWithNullFilmId(
            @RequestParam(required = false, defaultValue = "10") Integer count) {
        return reviewService.getReviews(null, count);  // filmId = null
    }

    /**
     * Добавление лайка
     *
     * @param id     id отзыва в базе данных
     * @param userId id пользователя в базе данных
     */
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") String id,
                        @PathVariable("userId") Integer userId) {
        reviewService.addLike(id, userId);
    }

    /**
     * Добавление дизлайка
     *
     * @param id     id отзыва в базе данных
     * @param userId id пользователя в базе данных
     */
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") String id,
                           @PathVariable("userId") Integer userId) {
        reviewService.addDislike(id, userId);
    }

    /**
     * Удаление лайка
     *
     * @param id     id отзыва в базе данных
     * @param userId id пользователя в базе данных
     */
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") String id,
                           @PathVariable("userId") Integer userId) {
        reviewService.removeRating(id, userId);
    }

    /**
     * Удаление дизлайка
     *
     * @param id     id отзыва в базе данных
     * @param userId id пользователя в базе данных
     */
    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable("id") String id,
                              @PathVariable("userId") Integer userId) {
        reviewService.removeRating(id, userId);
    }
}
