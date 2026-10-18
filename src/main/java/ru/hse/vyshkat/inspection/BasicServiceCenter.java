package ru.hse.vyshkat.inspection;

import ru.hse.vyshkat.model.Transport;

/** Принимает транспорт только с исправными тормозами и целой рамой. */
public final class BasicServiceCenter implements ServiceCenter {
    @Override
    public InspectionResult inspect(final Transport transport) {
        if (transport == null) {
            throw new IllegalArgumentException("Транспорт обязателен");
        }
        if (!transport.brakesOperational()) {
            return new InspectionResult(false, "Тормоза неисправны");
        }
        if (!transport.frameIntact()) {
            return new InspectionResult(false, "Рама повреждена");
        }
        return new InspectionResult(true, "Транспорт прошёл техосмотр");
    }
}
