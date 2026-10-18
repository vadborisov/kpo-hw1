package ru.hse.vyshkat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hse.vyshkat.inspection.InspectionResult;
import ru.hse.vyshkat.inspection.ServiceCenter;
import ru.hse.vyshkat.model.InventoryItem;
import ru.hse.vyshkat.model.Transport;
import ru.hse.vyshkat.model.equipment.ChargingStation;
import ru.hse.vyshkat.model.equipment.Helmet;
import ru.hse.vyshkat.model.transport.Bicycle;
import ru.hse.vyshkat.model.transport.ElectricBicycle;
import ru.hse.vyshkat.model.transport.ElectricScooter;

/** Поведение учёта парка с подменённым сервисом техосмотра. */
@DisplayName("Приёмка и отчёты парка")
class FleetServiceTest {
    private static final InspectionResult ACCEPTED =
            new InspectionResult(true, "Допущен тестовым сервисом");
    private static final InspectionResult REJECTED =
            new InspectionResult(false, "Отклонён тестовым сервисом");
    private final ServiceCenter center = mock(ServiceCenter.class);
    private final FleetService fleet = new FleetService(center);

    @Test
    @DisplayName("Приёмка использует решение зависимости и исходный объект")
    void acceptsAccordingToInjectedCenter() {
        final Transport transport = new Bicycle("Велосипед", "B1", 6,
                false, false);
        when(center.inspect(transport)).thenReturn(ACCEPTED);
        assertSame(ACCEPTED, fleet.addTransport(transport));
        assertSame(transport, fleet.inventory().getFirst());
        verify(center).inspect(transport);
    }

    @Test
    @DisplayName("Отказ исключает транспорт из отчётов и не занимает номер")
    void keepsRejectedTransportOutOfFleet() {
        final Transport transport = scooter("S1", "100");
        when(center.inspect(transport)).thenReturn(REJECTED, ACCEPTED);
        assertSame(REJECTED, fleet.addTransport(transport));
        assertTrue(fleet.inventory().isEmpty());
        assertTrue(fleet.beginnerTransports().isEmpty());
        assertEquals(new FleetSummary(0, 0, BigDecimal.ZERO), fleet.summary());
        assertSame(ACCEPTED, fleet.addTransport(transport));
        assertEquals(List.of(transport), fleet.inventory());
        verify(center, times(2)).inspect(transport);
    }

    @Test
    @DisplayName("Шлем и зарядная станция ставятся на баланс без техосмотра")
    void addsEquipmentWithoutInspection() {
        fleet.addEquipment(new Helmet("Шлем", "H1"));
        fleet.addEquipment(new ChargingStation("Станция", "C1",
                new BigDecimal("0.3")));
        assertEquals(new FleetSummary(0, 2, new BigDecimal("0.3")),
                fleet.summary());
        assertTrue(fleet.beginnerTransports().isEmpty());
        verifyNoInteractions(center);
    }

    @Test
    @DisplayName("Номер вещи запрещает приёмку транспорта ещё до техосмотра")
    void preventsTransportFromReplacingEquipment() {
        final Helmet helmet = new Helmet("Шлем", "001");
        fleet.addEquipment(helmet);
        assertThrows(IllegalArgumentException.class,
                () -> fleet.addTransport(scooter("001", "1")));
        assertEquals(List.of(helmet), fleet.inventory());
        verifyNoInteractions(center);
    }

    @Test
    @DisplayName("Повторный номер не заменяет принятый транспорт или вещь")
    void preservesExistingObjectsOnDuplicates() {
        final Transport transport = scooter("S1", "1");
        final Helmet helmet = new Helmet("Шлем", "H1");
        when(center.inspect(transport)).thenReturn(ACCEPTED);
        fleet.addTransport(transport);
        fleet.addEquipment(helmet);
        assertThrows(IllegalArgumentException.class,
                () -> fleet.addEquipment(new Helmet("Другой шлем", "S1")));
        assertThrows(IllegalArgumentException.class,
                () -> fleet.addEquipment(new Helmet("Другой шлем", "H1")));
        assertThrows(IllegalArgumentException.class,
                () -> fleet.addTransport(transport));
        assertEquals(List.of(transport, helmet), fleet.inventory());
        verify(center).inspect(transport);
    }

    @Test
    @DisplayName("Новичкам доступны только принятые устройства с простотой ≥6")
    void selectsBeginnerTransportsAtBoundary() {
        when(center.inspect(any())).thenReturn(ACCEPTED);
        fleet.addTransport(bicycle("B1", 1));
        fleet.addTransport(bicycle("B5", 5));
        final Transport six = bicycle("B6", 6);
        final Transport ten = bicycle("B10", 10);
        fleet.addTransport(six);
        fleet.addTransport(ten);
        fleet.addEquipment(new Helmet("Шлем", "H1"));
        assertEquals(List.of(six, ten), fleet.beginnerTransports());
    }

    @Test
    @DisplayName("Энергия суммируется для транспорта и вещей без округления")
    void sumsEnergyAcrossInventory() {
        when(center.inspect(any())).thenReturn(ACCEPTED);
        fleet.addTransport(scooter("S1", "0.1"));
        fleet.addTransport(new ElectricBicycle("Электровелосипед", "E1", 7,
                true, true, new BigDecimal("0.2")));
        fleet.addTransport(bicycle("B1", 5));
        fleet.addEquipment(new ChargingStation("Станция", "C1",
                new BigDecimal("0.3")));
        fleet.addEquipment(new Helmet("Шлем", "H1"));
        assertEquals(new FleetSummary(3, 2, new BigDecimal("0.6")),
                fleet.summary());
    }

    @Test
    @DisplayName("Списки являются неизменяемыми снимками и сохраняют порядок")
    void protectsInventorySnapshots() {
        when(center.inspect(any())).thenReturn(ACCEPTED);
        final Transport transport = bicycle("B1", 6);
        fleet.addTransport(transport);
        final List<InventoryItem> items = fleet.inventory();
        final List<Transport> beginners = fleet.beginnerTransports();
        assertThrows(UnsupportedOperationException.class, items::clear);
        assertThrows(UnsupportedOperationException.class, beginners::clear);
        fleet.addEquipment(new Helmet("Шлем", "H1"));
        fleet.addTransport(bicycle("B2", 10));
        assertEquals(List.of(transport), items);
        assertEquals(List.of(transport), beginners);
        assertEquals(List.of("B1", "H1", "B2"), fleet.inventory().stream()
                .map(InventoryItem::inventoryNumber).toList());
    }

    @Test
    @DisplayName("Пустой парк имеет нулевые показатели")
    void reportsEmptyFleet() {
        assertEquals(new FleetSummary(0, 0, BigDecimal.ZERO), fleet.summary());
        assertTrue(fleet.inventory().isEmpty());
        assertTrue(fleet.beginnerTransports().isEmpty());
        verifyNoInteractions(center);
    }

    @Test
    @DisplayName("Отсутствующие объекты отклоняются до вызова техосмотра")
    void rejectsMissingItems() {
        assertThrows(IllegalArgumentException.class,
                () -> fleet.addTransport(null));
        assertThrows(IllegalArgumentException.class,
                () -> fleet.addEquipment(null));
        verifyNoInteractions(center);
    }

    @Test
    @DisplayName("Сбой техосмотра не меняет баланс и не занимает номер")
    void preservesFleetWhenInspectionFails() {
        final Transport transport = scooter("S1", "1");
        when(center.inspect(transport)).thenThrow(
                new IllegalStateException("Техосмотр недоступен"))
                .thenReturn(ACCEPTED);
        assertThrows(IllegalStateException.class,
                () -> fleet.addTransport(transport));
        assertFalse(fleet.inventory().contains(transport));
        fleet.addTransport(transport);
        assertEquals(List.of(transport), fleet.inventory());
    }

    private static Transport bicycle(final String number, final int ease) {
        return new Bicycle("Велосипед", number, ease, true, true);
    }

    private static Transport scooter(final String number, final String energy) {
        return new ElectricScooter("Самокат", number, 7, true, true,
                new BigDecimal(energy));
    }
}
