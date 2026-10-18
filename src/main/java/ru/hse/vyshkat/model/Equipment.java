package ru.hse.vyshkat.model;

import ru.hse.vyshkat.model.validation.ModelValidation;

/** Общие данные вещи на балансе; техосмотр транспорта к ней не относится. */
public abstract class Equipment implements InventoryItem {
    private final String name;
    private final String inventoryNumber;

    /**
     * Создаёт вещь с обязательными учётными данными.
     * @param itemName название
     * @param number инвентарный номер
     */
    protected Equipment(final String itemName, final String number) {
        name = ModelValidation.requiredText(itemName, "Название");
        inventoryNumber = ModelValidation.requiredText(number,
                "Инвентарное обозначение");
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final String inventoryNumber() {
        return inventoryNumber;
    }
}
