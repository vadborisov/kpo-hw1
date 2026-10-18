package ru.hse.vyshkat.service;

import java.math.BigDecimal;

/**
 * Снимок общего состояния баланса на момент запроса.
 * @param transportCount количество принятого транспорта
 * @param equipmentCount количество вещей
 * @param dailyEnergyKwh общий суточный расход в кВт·ч
 */
public record FleetSummary(int transportCount, int equipmentCount,
                           BigDecimal dailyEnergyKwh) {
}
