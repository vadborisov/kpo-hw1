package ru.hse.vyshkat.model;

/** Объект на балансе с обязательным инвентарным номером. */
public interface InventoryItem {
    /** @return название объекта */
    String name();

    /** @return инвентарный номер */
    String inventoryNumber();
}
