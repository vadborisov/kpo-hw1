package ru.hse.vyshkat.model.transport;

import ru.hse.vyshkat.model.Transport;

/** Велосипед для проката. */
public final class Bicycle extends Transport {
    /**
     * Создаёт транспорт с характеристиками для последующего техосмотра.
     * @param name название
     * @param number инвентарный номер
     * @param ease простота для новичка от 1 до 10
     * @param workingBrakes исправны ли тормоза
     * @param intactFrame цела ли рама
     */
    public Bicycle(final String name, final String number,
                           final int ease, final boolean workingBrakes,
                           final boolean intactFrame) {
        super(name, number, ease, workingBrakes, intactFrame);
    }
}
