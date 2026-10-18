package ru.hse.vyshkat.model;

import ru.hse.vyshkat.model.validation.ModelValidation;

/** Общие данные транспорта до или после приёмки в парк. */
public abstract class Transport implements InventoryItem {
    private static final int MIN_EASE = 1;
    private static final int MAX_EASE = 10;
    private final String name;
    private final String inventoryNumber;
    private final int beginnerEase;
    private final boolean brakesOperational;
    private final boolean frameIntact;

    /**
     * Создаёт транспорт
     * @param transportName название
     * @param number инвентарный номер
     * @param ease простота от 1 до 10
     * @param workingBrakes исправны ли тормоза
     * @param intactFrame цела ли рама
     */
    protected Transport(final String transportName, final String number,
                        final int ease, final boolean workingBrakes,
                        final boolean intactFrame) {
        name = ModelValidation.requiredText(transportName, "Название");
        inventoryNumber = ModelValidation.requiredText(number,
                "Инвентарное обозначение");
        if (ease < MIN_EASE || ease > MAX_EASE) {
            throw new IllegalArgumentException(
                    "Простота для новичка должна быть от 1 до 10");
        }
        beginnerEase = ease;
        brakesOperational = workingBrakes;
        frameIntact = intactFrame;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final String inventoryNumber() {
        return inventoryNumber;
    }

    /** @return простота от 1 до 10 */
    public final int beginnerEase() {
        return beginnerEase;
    }

    /** @return исправны ли тормоза */
    public final boolean brakesOperational() {
        return brakesOperational;
    }

    /** @return цела ли рама */
    public final boolean frameIntact() {
        return frameIntact;
    }
}
