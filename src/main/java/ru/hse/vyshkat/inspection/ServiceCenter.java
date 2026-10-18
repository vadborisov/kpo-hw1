package ru.hse.vyshkat.inspection;

import ru.hse.vyshkat.model.Transport;

/** Проверяет транспорт перед приёмкой в активный парк. */
public interface ServiceCenter {
    /**
     * Принимает решение о технической исправности транспорта.
     * @param transport проверяемый транспорт
     * @return решение и его причина
     */
    InspectionResult inspect(Transport transport);
}
