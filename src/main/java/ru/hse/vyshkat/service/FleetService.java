package ru.hse.vyshkat.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.hse.vyshkat.inspection.InspectionResult;
import ru.hse.vyshkat.inspection.ServiceCenter;
import ru.hse.vyshkat.model.EnergyConsumer;
import ru.hse.vyshkat.model.Equipment;
import ru.hse.vyshkat.model.InventoryItem;
import ru.hse.vyshkat.model.Transport;

/** Принимает объекты на баланс и формирует отчёты по активному парку. */
public final class FleetService {
    private static final int BEGINNER_THRESHOLD = 6;
    private final ServiceCenter serviceCenter;
    private final Map<String, InventoryItem> inventory = new LinkedHashMap<>();

    /**
     * Создаёт учёт парка с подменяемым сервисом техосмотра.
     * @param inspectionCenter сервис техосмотра
     */
    public FleetService(final ServiceCenter inspectionCenter) {
        serviceCenter = Objects.requireNonNull(inspectionCenter,
                "Сервис техосмотра обязателен");
    }

    /**
     * Сохраняет транспорт только после положительного решения техосмотра.
     * Отклонённый транспорт не занимает инвентарный номер.
     * @param transport транспорт для приёмки
     * @return решение техосмотра
     */
    public InspectionResult addTransport(final Transport transport) {
        requireAvailableNumber(transport);
        final InspectionResult result = Objects.requireNonNull(
                serviceCenter.inspect(transport),
                "Сервис техосмотра должен вернуть решение");
        if (result.accepted()) {
            inventory.put(transport.inventoryNumber(), transport);
        }
        return result;
    }

    /**
     * Ставит вещь на баланс без техосмотра транспорта.
     * @param equipment вещь для учёта
     */
    public void addEquipment(final Equipment equipment) {
        requireAvailableNumber(equipment);
        inventory.put(equipment.inventoryNumber(), equipment);
    }

    /** @return неизменяемый снимок баланса в порядке добавления */
    public List<InventoryItem> inventory() {
        return List.copyOf(inventory.values());
    }

    /** @return принятый транспорт с простотой для новичка не ниже шести */
    public List<Transport> beginnerTransports() {
        return inventory.values().stream()
                .filter(Transport.class::isInstance)
                .map(Transport.class::cast)
                .filter(transport -> transport.beginnerEase()
                        >= BEGINNER_THRESHOLD)
                .toList();
    }

    /** @return количество транспорта, вещей и суммарный расход энергии */
    public FleetSummary summary() {
        final int transportCount = (int) inventory.values().stream()
                .filter(Transport.class::isInstance)
                .count();
        final BigDecimal energy = inventory.values().stream()
                .filter(EnergyConsumer.class::isInstance)
                .map(EnergyConsumer.class::cast)
                .map(EnergyConsumer::dailyEnergyKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FleetSummary(transportCount,
                inventory.size() - transportCount, energy);
    }

    private void requireAvailableNumber(final InventoryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Объект учёта обязателен");
        }
        if (inventory.containsKey(item.inventoryNumber())) {
            throw new IllegalArgumentException("Инвентарный номер уже занят: "
                    + item.inventoryNumber());
        }
    }
}
