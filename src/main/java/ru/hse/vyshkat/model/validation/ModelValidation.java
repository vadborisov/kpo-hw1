package ru.hse.vyshkat.model.validation;

import java.math.BigDecimal;

/** Общие проверки значений при создании модели. */
public final class ModelValidation {
    private ModelValidation() {
    }

    /**
     * Проверяет обязательную строку и убирает крайние пробелы.
     * @param value исходное значение
     * @param field название поля для сообщения об ошибке
     * @return непустая строка
     */
    public static String requiredText(final String value, final String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " обязательно");
        }
        return value.strip();
    }

    /**
     * Проверяет суточное энергопотребление.
     * @param value расход в кВт/ч
     * @return расход после проверки
     */
    // проверяем суточное энергопотребление
    public static BigDecimal energy(final BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(
                    "Расход энергии должен быть неотрицательным");
        }
        return value;
    }
}
