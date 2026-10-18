package ru.hse.vyshkat.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.hse.vyshkat.model.equipment.ChargingStation;
import ru.hse.vyshkat.model.equipment.Helmet;
import ru.hse.vyshkat.model.transport.Bicycle;
import ru.hse.vyshkat.model.transport.ElectricBicycle;
import ru.hse.vyshkat.model.transport.ElectricScooter;

/** Проверки инвариантов модели через её публичные конструкторы. */
@DisplayName("Модель транспорта и вещей")
class InventoryModelTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    @DisplayName("Пустое название и номер отклоняются у каждого вида объекта")
    void rejectsMissingIdentity(final String value) {
        for (BiFunction<String, String, InventoryItem> factory : factories()) {
            assertThrows(IllegalArgumentException.class,
                    () -> factory.apply(value, "001"));
            assertThrows(IllegalArgumentException.class,
                    () -> factory.apply("Название", value));
        }
    }

    @Test
    @DisplayName("Крайние пробелы убираются, ведущие нули номера сохраняются")
    void normalizesIdentity() {
        for (BiFunction<String, String, InventoryItem> factory : factories()) {
            final InventoryItem item = factory.apply("  Прокат  ", " 001 ");
            assertEquals("Прокат", item.name());
            assertEquals("001", item.inventoryNumber());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 11})
    @DisplayName("Простота вне диапазона 1–10 отклоняется у всего транспорта")
    void rejectsInvalidEase(final int ease) {
        assertThrows(IllegalArgumentException.class,
                () -> new Bicycle("Велосипед", "B1", ease, true, true));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricBicycle("Электровелосипед", "E1", ease,
                        true, true, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> new ElectricScooter("Самокат", "S1", ease,
                        true, true, BigDecimal.ONE));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 6, 10})
    @DisplayName("Допустимая простота сохраняется, включая границы диапазона")
    void acceptsValidEase(final int ease) {
        final List<Transport> transports = List.of(
                new Bicycle("Велосипед", "B1", ease, true, true),
                new ElectricBicycle("Электровелосипед", "E1", ease,
                        true, true, BigDecimal.ONE),
                new ElectricScooter("Самокат", "S1", ease,
                        true, true, BigDecimal.ONE));
        for (Transport transport : transports) {
            assertEquals(ease, transport.beginnerEase());
        }
    }

    @ParameterizedTest
    @MethodSource("conditions")
    @DisplayName("Модель сохраняет неисправности для последующего техосмотра")
    void preservesCondition(final boolean brakes, final boolean frame) {
        final List<Transport> transports = List.of(
                new Bicycle("Велосипед", "B1", 6, brakes, frame),
                new ElectricBicycle("Электровелосипед", "E1", 6,
                        brakes, frame, BigDecimal.ONE),
                new ElectricScooter("Самокат", "S1", 6,
                        brakes, frame, BigDecimal.ONE));
        for (Transport transport : transports) {
            assertEquals(brakes, transport.brakesOperational());
            assertEquals(frame, transport.frameIntact());
        }
    }

    @Test
    @DisplayName("Нулевой и дробный расход допустимы и сохраняются без потерь")
    void preservesEnergy() {
        for (Function<BigDecimal, EnergyConsumer> factory : consumers()) {
            assertEquals(BigDecimal.ZERO,
                    factory.apply(BigDecimal.ZERO).dailyEnergyKwh());
            final BigDecimal energy = new BigDecimal("0.125");
            assertEquals(energy, factory.apply(energy).dailyEnergyKwh());
        }
    }

    @Test
    @DisplayName("Отсутствующий и отрицательный расход энергии отклоняются")
    void rejectsInvalidEnergy() {
        for (Function<BigDecimal, EnergyConsumer> factory : consumers()) {
            assertThrows(IllegalArgumentException.class,
                    () -> factory.apply(null));
            assertThrows(IllegalArgumentException.class,
                    () -> factory.apply(new BigDecimal("-0.001")));
        }
    }

    @Test
    @DisplayName("Энергия есть у электротехники, но не у велосипеда и шлема")
    void separatesInventoryAndEnergy() {
        final InventoryItem bicycle = new Bicycle("Велосипед", "B1",
                6, true, true);
        final InventoryItem helmet = new Helmet("Шлем", "H1");
        final InventoryItem station = new ChargingStation("Станция", "C1",
                BigDecimal.ONE);
        assertFalse(bicycle instanceof EnergyConsumer);
        assertFalse(helmet instanceof EnergyConsumer);
        assertTrue(station instanceof EnergyConsumer);
        assertTrue(station instanceof Equipment);
        assertFalse(station instanceof Transport);
    }

    private static Stream<Arguments> conditions() {
        return Stream.of(Arguments.of(true, true), Arguments.of(false, true),
                Arguments.of(true, false), Arguments.of(false, false));
    }

    private static List<BiFunction<String, String, InventoryItem>> factories() {
        return List.of(Helmet::new,
                (name, number) -> new ChargingStation(name, number,
                        BigDecimal.ONE),
                (name, number) -> new Bicycle(name, number, 6, true, true),
                (name, number) -> new ElectricBicycle(name, number, 6,
                        true, true, BigDecimal.ONE),
                (name, number) -> new ElectricScooter(name, number, 6,
                        true, true, BigDecimal.ONE));
    }

    private static List<Function<BigDecimal, EnergyConsumer>> consumers() {
        return List.of(energy -> new ChargingStation("Станция", "C1", energy),
                energy -> new ElectricBicycle("Велосипед", "E1", 6,
                        true, true, energy),
                energy -> new ElectricScooter("Самокат", "S1", 6,
                        true, true, energy));
    }
}
