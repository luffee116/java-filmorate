package ru.yandex.practicum.filmorate.model.enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumUtils {

    private static final Logger log = LoggerFactory.getLogger(EnumUtils.class);

    /**
     * Безопасно преобразует строку в enum.
     *
     * @param enumClass Тип enum-класса
     * @param value     Строка для преобразования
     * @return Соответствующий enum или null, если значение некорректное
     */
    public static <T extends Enum<T>> T safeValueOf(Class<T> enumClass, String value) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid enum value for {}: {}", enumClass.getSimpleName(), value);
            return null;
        }
    }
}