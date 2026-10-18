package ru.hse.vyshkat.inspection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hse.vyshkat.model.transport.Bicycle;

/** Поведение стандартного техосмотра. */
@DisplayName("Техосмотр тормозов и рамы")
class BasicServiceCenterTest {
    private final ServiceCenter center = new BasicServiceCenter();

    @ParameterizedTest
    @CsvSource({
        "true, true, true, Транспорт прошёл техосмотр",
        "false, true, false, Тормоза неисправны",
        "true, false, false, Рама повреждена",
        "false, false, false, Тормоза неисправны"
    })
    @DisplayName("Приёмка требует исправности обоих узлов и поясняет отказ")
    void checksBothComponents(final boolean brakes, final boolean frame,
                             final boolean accepted, final String reason) {
        final InspectionResult result = center.inspect(
                new Bicycle("Велосипед", "B1", 6, brakes, frame));
        assertEquals(accepted, result.accepted());
        assertEquals(reason, result.reason());
    }

    @Test
    @DisplayName("Отсутствующий транспорт не может пройти техосмотр")
    void rejectsMissingTransport() {
        assertThrows(IllegalArgumentException.class,
                () -> center.inspect(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("Решение техосмотра требует пояснения")
    void requiresReason(final String reason) {
        assertThrows(IllegalArgumentException.class,
                () -> new InspectionResult(false, reason));
    }
}
