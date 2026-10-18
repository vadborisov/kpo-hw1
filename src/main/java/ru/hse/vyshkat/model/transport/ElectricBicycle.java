package ru.hse.vyshkat.model.transport;

import java.math.BigDecimal;
import ru.hse.vyshkat.model.EnergyConsumer;
import ru.hse.vyshkat.model.Transport;
import ru.hse.vyshkat.model.validation.ModelValidation;

/** Электровелосипед для проката. */
public final class ElectricBicycle extends Transport implements EnergyConsumer {
    private final BigDecimal dailyEnergyKwh;

    /**
     * Создаёт транспорт с характеристиками для последующего техосмотра.
     * @param name название
     * @param number инвентарный номер
     * @param ease простота для новичка от 1 до 10
     * @param workingBrakes исправны ли тормоза
     * @param intactFrame цела ли рама
     * @param energy суточный расход в кВт·ч
     */
    public ElectricBicycle(final String name, final String number,
                           final int ease, final boolean workingBrakes,
                           final boolean intactFrame,
                           final BigDecimal energy) {
        super(name, number, ease, workingBrakes, intactFrame);
        dailyEnergyKwh = ModelValidation.energy(energy);
    }

    @Override
    public BigDecimal dailyEnergyKwh() {
        return dailyEnergyKwh;
    }
}
