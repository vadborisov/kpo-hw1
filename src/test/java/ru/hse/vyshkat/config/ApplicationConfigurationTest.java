package ru.hse.vyshkat.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.hse.vyshkat.inspection.InspectionResult;
import ru.hse.vyshkat.inspection.ServiceCenter;
import ru.hse.vyshkat.model.transport.Bicycle;
import ru.hse.vyshkat.service.FleetService;

/** Проверяет создание и подмену зависимостей в настоящем DI-контейнере. */
@DisplayName("Сборка приложения через Spring Context")
class ApplicationConfigurationTest {
    @Test
    @DisplayName("Контейнер связывает учёт парка с настоящим техосмотром")
    void wiresRealServiceCenter() {
        try (var context = new AnnotationConfigApplicationContext(
                ApplicationConfiguration.class)) {
            final FleetService fleet = context.getBean(FleetService.class);
            assertTrue(fleet.addTransport(new Bicycle("Исправный", "B1", 6,
                    true, true)).accepted());
            assertFalse(fleet.addTransport(new Bicycle("Неисправный", "B2", 6,
                    false, true)).accepted());
            assertEquals(1, fleet.summary().transportCount());
        }
    }

    @Test
    @DisplayName("Тестовый бин техосмотра заменяет решение стандартного")
    void replacesCenterInsideContainer() {
        try (var context = new AnnotationConfigApplicationContext(
                ApplicationConfiguration.class, RejectionConfiguration.class)) {
            final FleetService fleet = context.getBean(FleetService.class);
            final InspectionResult result = fleet.addTransport(new Bicycle(
                    "Исправный", "B1", 6, true, true));
            assertFalse(result.accepted());
            assertEquals("Отказ тестового техосмотра", result.reason());
            assertTrue(fleet.inventory().isEmpty());
        }
    }

    /** Тестовая конфигурация с приоритетной подменой зависимости. */
    @Configuration(proxyBeanMethods = false)
    static class RejectionConfiguration {
        /** @return сервис, отклоняющий любой транспорт */
        @Bean
        @Primary
        ServiceCenter rejectingServiceCenter() {
            return transport -> new InspectionResult(false,
                    "Отказ тестового техосмотра");
        }
    }
}
