package ru.hse.vyshkat.model.equipment;

import java.math.BigDecimal;
import ru.hse.vyshkat.model.EnergyConsumer;
import ru.hse.vyshkat.model.Equipment;
import ru.hse.vyshkat.model.validation.ModelValidation;

/** Зарядная станция учитывается как вещь и потребитель энергии. */
public final class ChargingStation extends Equipment implements EnergyConsumer {
    private final BigDecimal dailyEnergyKwh;

    /**
     * Создаёт зарядную станцию.
     * @param name название
     * @param number инвентарный номер
     * @param energy суточный расход в кВт·ч
     */
    public ChargingStation(final String name, final String number,
                           final BigDecimal energy) {
        super(name, number);
        dailyEnergyKwh = ModelValidation.energy(energy);
    }

    @Override
    public BigDecimal dailyEnergyKwh() {
        return dailyEnergyKwh;
    }
}
