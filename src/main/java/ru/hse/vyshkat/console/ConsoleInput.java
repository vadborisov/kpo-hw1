package ru.hse.vyshkat.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/** Читает значения построчно и повторяет запрос при ошибочном вводе. */
final class ConsoleInput {
    private final Scanner scanner;
    private final PrintStream output;

    ConsoleInput(final InputStream input, final PrintStream target) {
        scanner = new Scanner(input, StandardCharsets.UTF_8);
        output = target;
    }

    String line(final String prompt) {
        output.print(prompt);
        output.flush();
        return scanner.nextLine().strip();
    }

    String text(final String prompt) {
        while (true) {
            final String value = line(prompt);
            if (!value.isBlank()) {
                return value;
            }
            output.println("Значение не должно быть пустым.");
        }
    }

    String choice(final String prompt, final Set<String> options) {
        while (true) {
            final String value = line(prompt);
            if (options.contains(value)) {
                return value;
            }
            output.println("Выберите один из предложенных вариантов.");
        }
    }

    int integer(final String prompt, final int min, final int max) {
        while (true) {
            try {
                final int value = Integer.parseInt(line(prompt));
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException exception) {
                // Ошибка формата обрабатывается тем же повторным запросом.
            }
            output.printf("Введите целое число от %d до %d.%n", min, max);
        }
    }

    boolean yesNo(final String prompt) {
        while (true) {
            final String value = line(prompt).toLowerCase(Locale.ROOT);
            switch (value) {
                case "да", "1" -> {
                    return true;
                }
                case "нет", "0" -> {
                    return false;
                }
                default -> output.println("Введите да или нет (1 или 0).");
            }
        }
    }

    BigDecimal energy(final String prompt) {
        while (true) {
            try {
                final BigDecimal value = new BigDecimal(
                        line(prompt).replace(',', '.'));
                if (value.signum() >= 0) {
                    return value;
                }
            } catch (NumberFormatException exception) {
                // Ошибка формата обрабатывается тем же повторным запросом.
            }
            output.println("Введите неотрицательное число, например 0.5.");
        }
    }
}
