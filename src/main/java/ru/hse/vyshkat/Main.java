package ru.hse.vyshkat;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.hse.vyshkat.config.ApplicationConfiguration;
import ru.hse.vyshkat.console.ConsoleMenu;

/** Точка входа: создаёт контейнер и запускает консольное приложение. */
public final class Main {
    private Main() {
    }

    /**
     * Запускает интерактивное меню.
     * @param args аргументы командной строки, не используются
     */
    public static void main(final String[] args) {
        try (var context = new AnnotationConfigApplicationContext(
                ApplicationConfiguration.class)) {
            context.getBean(ConsoleMenu.class).run();
        }
    }
}
