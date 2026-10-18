package ru.hse.vyshkat.console;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import ru.hse.vyshkat.inspection.InspectionResult;
import ru.hse.vyshkat.model.Equipment;
import ru.hse.vyshkat.model.InventoryItem;
import ru.hse.vyshkat.model.Transport;
import ru.hse.vyshkat.model.equipment.ChargingStation;
import ru.hse.vyshkat.model.equipment.Helmet;
import ru.hse.vyshkat.model.transport.Bicycle;
import ru.hse.vyshkat.model.transport.ElectricBicycle;
import ru.hse.vyshkat.model.transport.ElectricScooter;
import ru.hse.vyshkat.service.FleetService;
import ru.hse.vyshkat.service.FleetSummary;

/** Управляет диалогом с пользователем, делегируя учёт сервису парка. */
public final class ConsoleMenu {
    private static final int MIN_EASE = 1;
    private static final int MAX_EASE = 10;
    private final FleetService fleet;
    private final ConsoleInput input;
    private final PrintStream output;

    /**
     * Создаёт меню с подменяемыми сервисом и потоками для тестирования.
     * @param fleetService учёт парка
     * @param source поток ввода в UTF-8
     * @param target поток вывода
     */
    public ConsoleMenu(final FleetService fleetService,
                       final InputStream source, final PrintStream target) {
        fleet = Objects.requireNonNull(fleetService);
        output = Objects.requireNonNull(target);
        input = new ConsoleInput(Objects.requireNonNull(source), output);
    }

    /** Запускает диалог до команды выхода или окончания входного потока. */
    public void run() {
        output.println("ВышКат — учёт парка");
        output.println("Данные хранятся в памяти до завершения программы.");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                final String command = input.line("Команда: ");
                try {
                    switch (command) {
                        case "1" -> addTransport();
                        case "2" -> addEquipment();
                        case "3" -> printSummary();
                        case "4" -> printBeginners();
                        case "5" -> printInventory();
                        case "0" -> running = false;
                        default -> output.println("Неизвестная команда.");
                    }
                } catch (IllegalArgumentException exception) {
                    output.println("Ошибка: " + exception.getMessage());
                }
            }
            output.println("До встречи!");
        } catch (NoSuchElementException exception) {
            output.println("\nВвод завершён. До встречи!");
        }
    }

    private void printMenu() {
        output.println();
        output.println("1 — Добавить транспорт");
        output.println("2 — Добавить вещь");
        output.println("3 — Отчёт о количестве и энергии");
        output.println("4 — Транспорт для новичков");
        output.println("5 — Весь баланс: названия и номера");
        output.println("0 — Выход");
    }

    private void addTransport() {
        output.println("1 — Электросамокат; 2 — Велосипед; "
                + "3 — Электровелосипед");
        final String type = input.choice("Вид транспорта: ",
                Set.of("1", "2", "3"));
        final String name = input.text("Название: ");
        final String number = input.text("Инвентарный номер: ");
        final int ease = input.integer("Простота для новичка (1–10): ",
                MIN_EASE, MAX_EASE);
        final boolean brakes = input.yesNo("Тормоза исправны (да/нет): ");
        final boolean frame = input.yesNo("Рама цела (да/нет): ");
        final Transport transport = switch (type) {
            case "1" -> new ElectricScooter(name, number, ease, brakes, frame,
                    input.energy("Суточный расход энергии (кВт·ч): "));
            case "2" -> new Bicycle(name, number, ease, brakes, frame);
            case "3" -> new ElectricBicycle(name, number, ease, brakes, frame,
                    input.energy("Суточный расход энергии (кВт·ч): "));
            default -> throw new IllegalArgumentException("Неизвестный вид");
        };
        final InspectionResult result = fleet.addTransport(transport);
        output.println((result.accepted() ? "Принят: " : "Отклонён: ")
                + result.reason());
    }

    private void addEquipment() {
        output.println("1 — Шлем; 2 — Зарядная станция");
        final String type = input.choice("Вид вещи: ", Set.of("1", "2"));
        final String name = input.text("Название: ");
        final String number = input.text("Инвентарный номер: ");
        final Equipment equipment = switch (type) {
            case "1" -> new Helmet(name, number);
            case "2" -> new ChargingStation(name, number,
                    input.energy("Суточный расход энергии (кВт·ч): "));
            default -> throw new IllegalArgumentException("Неизвестный вид");
        };
        fleet.addEquipment(equipment);
        output.println("Вещь добавлена на баланс.");
    }

    private void printSummary() {
        final FleetSummary summary = fleet.summary();
        output.printf("Транспорт: %d%nВещи: %d%nВсего на балансе: %d%n",
                summary.transportCount(), summary.equipmentCount(),
                summary.transportCount() + summary.equipmentCount());
        output.println("Суточный расход: " + summary.dailyEnergyKwh()
                .stripTrailingZeros().toPlainString() + " кВт·ч");
    }

    private void printBeginners() {
        final List<Transport> transports = fleet.beginnerTransports();
        if (transports.isEmpty()) {
            output.println("Подходящего транспорта пока нет.");
            return;
        }
        output.println("Транспорт для новичков:");
        for (Transport transport : transports) {
            output.printf("%s — %s; простота: %d%n",
                    transport.inventoryNumber(), transport.name(),
                    transport.beginnerEase());
        }
    }

    private void printInventory() {
        final List<InventoryItem> items = fleet.inventory();
        if (items.isEmpty()) {
            output.println("Баланс пока пуст.");
            return;
        }
        output.println("Объекты на балансе:");
        for (InventoryItem item : items) {
            output.printf("%s — %s%n", item.inventoryNumber(), item.name());
        }
    }
}
