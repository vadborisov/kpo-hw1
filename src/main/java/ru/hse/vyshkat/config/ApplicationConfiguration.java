package ru.hse.vyshkat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.hse.vyshkat.console.ConsoleMenu;
import ru.hse.vyshkat.inspection.BasicServiceCenter;
import ru.hse.vyshkat.inspection.ServiceCenter;
import ru.hse.vyshkat.service.FleetService;

/** Явно связывает сервис учёта с реализацией техосмотра через Spring. */
@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {
    /** @return стандартный сервис техосмотра */
    @Bean
    ServiceCenter serviceCenter() {
        return new BasicServiceCenter();
    }

    /**
     * Создаёт сервис учёта с зависимостью из контейнера.
     * @param serviceCenter сервис техосмотра
     * @return учёт парка
     */
    @Bean
    FleetService fleetService(final ServiceCenter serviceCenter) {
        return new FleetService(serviceCenter);
    }
    /**
     * Подключает консоль к сервису парка и системным потокам.
     * @param fleetService учёт парка
     * @return интерактивное меню
     */
    @Bean
    ConsoleMenu consoleMenu(final FleetService fleetService) {
        return new ConsoleMenu(fleetService, System.in, System.out);
    }
}
