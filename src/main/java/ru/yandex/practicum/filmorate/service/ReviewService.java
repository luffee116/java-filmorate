package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exeptions.ConflictException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.dto.ReviewDtoMapper;
import ru.yandex.practicum.filmorate.mapper.toEntity.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewRatingStorage;
import ru.yandex.practicum.filmorate.repository.ReviewStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReviewService {
    ReviewStorage reviewStorage;
    ReviewRatingStorage reviewRatingStorage;
    FilmService filmService;
    UserService userService;

    /**
     * Конструктор ReviewService
     *
     * @param filmService         сервис фильмов
     * @param reviewStorage       сервис отзывов
     * @param reviewRatingStorage сервис обработки рейтинга отзывов
     * @param userService         сервис пользователей
     */
    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         ReviewRatingStorage reviewRatingStorage,
                         FilmService filmService,
                         UserService userService) {
        this.reviewStorage = reviewStorage;
        this.reviewRatingStorage = reviewRatingStorage;
        this.filmService = filmService;
        this.userService = userService;
    }

    /**
     * Добавление отзыва
     *
     * @param reviewDto входящий объект класса ReviewDto
     */
    public ReviewDto createReview(@Valid ReviewDto reviewDto) {
        checkUserAndFilmExist(reviewDto.getUserId(), reviewDto.getFilmId());

        if (reviewStorage.existsByUserIdAndFilmId(reviewDto.getUserId(), reviewDto.getFilmId())) {
            throw new ConflictException(
                    String.format(
                            "Пользователь с ID %d уже оставлял отзыв на фильм с ID %d",
                            reviewDto.getUserId(),
                            reviewDto.getFilmId()
                    )
            );
        }

        Review request = ReviewMapper.mapToReview(reviewDto);
        Review review = reviewStorage.save(request);
        return ReviewDtoMapper.mapToDto(review);
    }

    /**
     * Обновление отзыва
     *
     * @param reviewDto входящий объект класса ReviewDto
     */
    public ReviewDto updateReview(@Valid ReviewDto reviewDto) {
        checkReviewExist(reviewDto.getReviewId());
        Review request = ReviewMapper.mapToReview(reviewDto);
        Review review = reviewStorage.update(request);
        return ReviewDtoMapper.mapToDto(review);
    }

    /**
     * Удаление отзыва
     *
     * @param id id объекта Review
     */
    public void deleteReview(Integer id) {
        checkReviewExist(id);
        reviewStorage.removeById(id);
    }

    /**
     * Получение отзыва по id
     *
     * @param id id объекта Review
     */
    public Optional<ReviewDto> getReviewById(Integer id) {
        Optional<Review> review = reviewStorage.getReviewById(id);
        return Optional.of(ReviewDtoMapper.mapToDto(review.get()));
    }

    /**
     * Получение списка отзывов
     *
     * @param filmId id фильма
     * @param count  количество выводимых значений
     */
    public List<ReviewDto> getReviews(Integer filmId, Integer count) {
        if (filmId != null) {
            List<Review> response = reviewStorage.getReviewsById(filmId, count);
            return response.stream().map(ReviewDtoMapper::mapToDto).collect(Collectors.toList());
        } else {
            List<Review> getAllList = reviewStorage.getAll(count);
            return getAllList.stream().map(ReviewDtoMapper::mapToDto).collect(Collectors.toList());
        }
    }

    /**
     * Добавление лайка
     *
     * @param reviewId id отзыва
     * @param userId   id пользователя
     */
    public void addLike(String reviewId, Integer userId) {
        if (reviewId.matches("\\d+")) {
            processReviewRating(Integer.parseInt(reviewId), userId, true);
        } else {
            int lastReviewId = reviewStorage.getLastReviewId();
            processReviewRating(lastReviewId, userId, true);
        }
    }

    /**
     * Добавление дизлайка
     *
     * @param reviewId id отзыва
     * @param userId   id пользователя
     */
    public void addDislike(String reviewId, Integer userId) {
        if (reviewId.matches("\\d+")) {
            processReviewRating(Integer.parseInt(reviewId), userId, false);
        } else {
            int lastReviewId = reviewStorage.getLastReviewId();
            processReviewRating(lastReviewId, userId, false);
        }
    }

    /**
     * Удаление лайка / дизлайка
     *
     * @param reviewId id отзыва
     * @param userId   id пользователя
     */
    public void removeRating(String reviewId, Integer userId) {
        if (reviewId.matches("\\d+")) {
            deleteManager(Integer.parseInt(reviewId), userId);
        } else {
            int lastReviewId = reviewStorage.getLastReviewId();
            deleteManager(lastReviewId, userId);
        }
    }

    /**
     * Обновление рейтинга полезности отзыва
     */
    private void updateReviewUseful(Integer reviewId, boolean isLike, boolean isAdd) {
        int delta = isLike ? 1 : -1; // лайк = +1, дизлайк = -1
        if (!isAdd) {
            delta *= -1; // если удаляем оценку, меняем знак
        }
        reviewStorage.updateUseful(reviewId, delta); // атомарное обновление в БД
    }

    /**
     * Обработка оценки отзыва (лайк/дизлайк)
     *
     * @param reviewId id отзыва
     * @param userId   id пользователя
     * @param isLike   true - лайк / false - дизлайк
     */
    private void processReviewRating(Integer reviewId, Integer userId, boolean isLike) {
        Review review = checkReviewExist(reviewId);
        userService.getUserById(userId);

        Optional<Boolean> existingRating = reviewRatingStorage.getRating(reviewId, userId);

        if (existingRating.isPresent()) {
            boolean oldLike = existingRating.get();
            if (oldLike == isLike) {
                return; // оценка не изменилась
            }
            // Удаляем старую оценку (-1 или +1)
            updateReviewUseful(reviewId, oldLike, false);
            // Добавляем новую оценку (+1 или -1)
            updateReviewUseful(reviewId, isLike, true);
            // Обновляем оценку в БД
            reviewRatingStorage.updateRating(reviewId, userId, isLike);
        } else {
            // Просто добавляем новую оценку
            reviewRatingStorage.addRating(reviewId, userId, isLike);
            updateReviewUseful(reviewId, isLike, true);
        }
        log.info("{} {} к отзыву {}",
                isLike ? "Лайк" : "Дизлайк",
                existingRating.isPresent() ? "изменен" : "добавлен",
                reviewId);
    }

    /**
     * Проверка существования отзыва
     *
     * @param reviewId id отзыва
     */
    private Review checkReviewExist(Integer reviewId) {
        if (reviewId == null) {
            return null;
        }
        return reviewStorage.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Отзыв с id %d не найден", reviewId)));
    }

    /**
     * Проверка существования фильма и пользователя
     *
     * @param filmId id фильма
     * @param userId id пользователя
     */
    private void checkUserAndFilmExist(Integer userId, Integer filmId) {
        if (userId == null || filmId == null) {
            return;
        }
        userService.getUserById(userId);
        filmService.getFilmById(filmId);
    }

    /**
     * Получение количества всех отзывов
     */
    public int getLastReviewId() {
        return reviewStorage.getLastReviewId();
    }

    /**
     * Менеджер удаления отзыва
     *
     * @param reviewId id отзыва
     * @param userId   id пользователя
     */
    private void deleteManager(Integer reviewId, Integer userId) {
        Review review = checkReviewExist(reviewId);
        userService.getUserById(userId);

        Optional<Boolean> existingRating = reviewRatingStorage.getRating(reviewId, userId);
        if (existingRating.isEmpty()) {
            return;
        }
        reviewRatingStorage.deleteRating(reviewId, userId);
        updateReviewUseful(reviewId, existingRating.get(), false);
        log.info("Удалена оценка отзыва {} пользователем {}", reviewId, userId);
    }
}