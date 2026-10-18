package ru.hse.vyshkat.model;

import java.math.BigDecimal;

/** Устройство, потребляющее электрическую энергию. */
public interface EnergyConsumer {
    /** @return неотрицательный суточный расход в кВт·ч */
    BigDecimal dailyEnergyKwh();
}
