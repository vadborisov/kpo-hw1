package ru.hse.vyshkat.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hse.vyshkat.inspection.BasicServiceCenter;
import ru.hse.vyshkat.model.InventoryItem;
import ru.hse.vyshkat.service.FleetService;
import ru.hse.vyshkat.service.FleetSummary;

/** Сквозные сценарии консоли с настоящими моделью и сервисами. */
@DisplayName("Интерактивное меню ВышКат")
class ConsoleMenuTest {
    private final FleetService fleet = new FleetService(
            new BasicServiceCenter());

    @Test
    @DisplayName("Демонстрация проходит все пункты, приёмку, отказ и дубликат")
    void demonstratesAllOperations() throws IOException {
        final String script;
        try (var stream = Objects.requireNonNull(
                getClass().getResourceAsStream("/demo-input.txt"))) {
            script = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        final String output = run(script);
        assertEquals(new FleetSummary(3, 2, new BigDecimal("2.00")),
                fleet.summary());
        assertEquals(List.of("S-001", "E-001"), fleet.beginnerTransports()
                .stream().map(InventoryItem::inventoryNumber).toList());
        assertEquals(List.of("S-001", "B-001", "E-001", "H-001", "C-001"),
                fleet.inventory().stream()
                        .map(InventoryItem::inventoryNumber).toList());
        assertTrue(output.contains("Транспорт: 3"));
        assertTrue(output.contains("Вещи: 2"));
        assertTrue(output.contains("Суточный расход: 2 кВт·ч"));
        assertTrue(output.contains("Подходящего транспорта пока нет."));
        assertTrue(output.contains("Баланс пока пуст."));
        assertTrue(output.contains("Отклонён: Тормоза неисправны"));
        assertTrue(output.contains("Отклонён: Рама повреждена"));
        assertTrue(output.contains("Инвентарный номер уже занят: S-001"));
        assertTrue(output.contains("S-001 — Самокат у корпуса; простота: 6"));
        assertTrue(output.contains("H-001 — Защитный шлем"));
        assertTrue(output.contains("Неизвестная команда."));
        assertTrue(output.endsWith("До встречи!" + System.lineSeparator()));
    }

    @Test
    @DisplayName("Неверные поля транспорта запрашиваются повторно")
    void retriesInvalidTransportFields() {
        final String output = run("""
                1
                9
                2

                Велосипед

                B1
                ошибка
                0
                11
                2147483648
                6
                может быть
                ДА
                неизвестно
                нет
                0
                """);
        assertTrue(output.contains("Выберите один из предложенных вариантов"));
        assertTrue(output.contains("Значение не должно быть пустым"));
        assertTrue(output.contains("Введите целое число от 1 до 10"));
        assertTrue(output.contains("Введите да или нет"));
        assertTrue(output.contains("Отклонён: Рама повреждена"));
        assertTrue(fleet.inventory().isEmpty());
    }

    @Test
    @DisplayName("Ошибочный и отрицательный расход повторяют запрос")
    void retriesInvalidEnergy() {
        final String output = run("""
                2
                7
                2
                Станция
                C1
                текст
                -0.1
                NaN
                1,25
                3
                0
                """);
        assertEquals(new BigDecimal("1.25"), fleet.summary().dailyEnergyKwh());
        assertTrue(output.contains("Введите неотрицательное число"));
        assertTrue(output.contains("Суточный расход: 1.25 кВт·ч"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.125", "1,5", "1E+1"})
    @DisplayName("Нулевой, дробный и экспоненциальный расход читается точно")
    void readsEnergy(final String value) {
        run("2\n2\nСтанция\nC1\n" + value + "\n0\n");
        final BigDecimal expected = new BigDecimal(value.replace(',', '.'));
        assertEquals(0, expected.compareTo(fleet.summary().dailyEnergyKwh()));
    }

    @Test
    @DisplayName("Ответы 1 и 0 задают исправность и позволяют продолжить меню")
    void readsNumericCondition() {
        final String output = run("""
                1
                2
                Исправный
                B1
                1
                1
                1
                1
                2
                Неисправный
                B2
                10
                0
                1
                3
                0
                """);
        assertEquals(1, fleet.summary().transportCount());
        assertTrue(output.contains("Принят: Транспорт прошёл техосмотр"));
        assertTrue(output.contains("Отклонён: Тормоза неисправны"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1\n", "1\n2\nВелосипед\nB1\n6\nда\n",
        "2\n2\nСтанция\nC1\n"})
    @DisplayName("Окончание ввода не сохраняет незавершённый объект")
    void exitsAtEndOfInput(final String script) {
        final String output = run(script);
        assertTrue(fleet.inventory().isEmpty());
        assertTrue(output.contains("Ввод завершён. До встречи!"));
        assertFalse(output.contains("Exception"));
    }

    private String run(final String script) {
        final var buffer = new ByteArrayOutputStream();
        try (var output = new PrintStream(buffer, true,
                StandardCharsets.UTF_8)) {
            new ConsoleMenu(fleet, new ByteArrayInputStream(
                    script.getBytes(StandardCharsets.UTF_8)), output).run();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
