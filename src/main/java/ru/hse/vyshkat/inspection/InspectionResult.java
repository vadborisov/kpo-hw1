package ru.hse.vyshkat.inspection;

import ru.hse.vyshkat.model.validation.ModelValidation;

/**
 * Решение сервиса техосмотра с пояснением для пользователя.
 * @param accepted можно ли принять транспорт
 * @param reason причина решения
 */
public record InspectionResult(boolean accepted, String reason) {
    /** Проверяет обязательное пояснение решения. */
    public InspectionResult {
        reason = ModelValidation.requiredText(reason, "Пояснение решения");
    }
}
