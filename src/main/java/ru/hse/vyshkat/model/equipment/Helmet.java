package ru.hse.vyshkat.model.equipment;

import ru.hse.vyshkat.model.Equipment;

/** Шлем на балансе; электрическую энергию не потребляет. */
public final class Helmet extends Equipment {
    /**
     * Создаёт шлем.
     * @param name название
     * @param number инвентарный номер
     */
    public Helmet(final String name, final String number) {
        super(name, number);
    }
}
