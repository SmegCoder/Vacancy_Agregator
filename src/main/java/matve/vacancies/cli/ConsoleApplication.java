package matve.vacancies.cli;

import matve.vacancies.domain.*;
import matve.vacancies.export.ExportService;
import matve.vacancies.service.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApplication {
    private static final int DEFAULT_LIMIT = 20;

    private final SourceService sourceService;
    private final VacancyService vacancyService;
    private final AggregationService aggregationService;
    private final AlertService alertService;
    private final AnalyticsService analyticsService;
    private final HistoryService historyService;
    private final AutoRefreshService autoRefreshService;
    private final ExportService exportService;
    private final ConsolePrinter printer;

    public ConsoleApplication(SourceService sourceService,
                              VacancyService vacancyService,
                              AggregationService aggregationService,
                              AlertService alertService,
                              AnalyticsService analyticsService,
                              HistoryService historyService,
                              AutoRefreshService autoRefreshService,
                              ExportService exportService,
                              ConsolePrinter printer) {
        this.sourceService = sourceService;
        this.vacancyService = vacancyService;
        this.aggregationService = aggregationService;
        this.alertService = alertService;
        this.analyticsService = analyticsService;
        this.historyService = historyService;
        this.autoRefreshService = autoRefreshService;
        this.exportService = exportService;
        this.printer = printer;
    }

    public void run() {
        System.out.println("Vacancy Aggregator. Введите help для списка команд.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isBlank()) {
                continue;
            }
            try {
                boolean shouldExit = handle(line);
                if (shouldExit) {
                    autoRefreshService.stop();
                    System.out.println("Пока!");
                    return;
                }
            } catch (RuntimeException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private boolean handle(String line) {
        List<String> tokens = CommandTokenizer.tokenize(line);
        String command = tokens.get(0).toLowerCase();
        switch (command) {
            case "help" -> printer.printHelp();
            case "sources" -> printer.printSources(sourceService.all());
            case "add-source" -> addSource(tokens);
            case "enable-source" -> enableSource(tokens, true);
            case "disable-source" -> enableSource(tokens, false);
            case "delete-source" -> deleteSource(tokens);
            case "refresh" -> refresh();
            case "auto-refresh" -> startAutoRefresh(tokens);
            case "stop-auto-refresh" -> stopAutoRefresh();
            case "list" -> list(tokens);
            case "search" -> search(tokens);
            case "filter" -> filter(tokens);
            case "view" -> view(tokens);
            case "add-alert" -> addAlert(tokens);
            case "alerts" -> printer.printAlerts(alertService.all());
            case "export" -> export(tokens);
            case "stats" -> stats();
            case "history" -> history(tokens);
            case "exit", "quit" -> { return true; }
            default -> System.out.println("Неизвестная команда. Введите help.");
        }
        return false;
    }

    private void addSource(List<String> tokens) {
        require(tokens.size() >= 4, "Формат: add-source <name> <type> <url> [pages]");
        String name = tokens.get(1);
        SourceType type = SourceType.fromString(tokens.get(2));
        String url = tokens.get(3);
        int pages = tokens.size() >= 5 ? parseInt(tokens.get(4), "pages") : 1;
        Source source = sourceService.add(name, type, url, pages);
        System.out.println("Источник добавлен: id=" + source.getId());
    }

    private void enableSource(List<String> tokens, boolean enabled) {
        require(tokens.size() >= 2, "Формат: enable-source <id> или disable-source <id>");
        long id = parseLong(tokens.get(1), "id");
        if (enabled) {
            sourceService.enable(id);
            System.out.println("Источник включен.");
        } else {
            sourceService.disable(id);
            System.out.println("Источник выключен.");
        }
    }

    private void deleteSource(List<String> tokens) {
        require(tokens.size() >= 2, "Формат: delete-source <id>");
        sourceService.delete(parseLong(tokens.get(1), "id"));
        System.out.println("Источник удален.");
    }

    private void refresh() {
        AggregationResult result = aggregationService.refreshAll();
        List<String> notifications = alertService.matchNewVacancies(result.getAdded());
        printer.printAggregation(result, notifications);
    }

    private void startAutoRefresh(List<String> tokens) {
        require(tokens.size() >= 2, "Формат: auto-refresh <seconds>");
        long seconds = parseLong(tokens.get(1), "seconds");
        boolean started = autoRefreshService.start(seconds, new AutoRefreshService.Output() {
            @Override
            public void println(String text) {
                System.out.println(text);
            }

            @Override
            public void printPrompt() {
                System.out.print("> ");
            }
        });
        if (started) {
            System.out.println("Автообновление включено. Интервал: " + Math.max(10, seconds) + " сек.");
        } else {
            System.out.println("Автообновление уже запущено.");
        }
    }

    private void stopAutoRefresh() {
        autoRefreshService.stop();
        System.out.println("Автообновление выключено.");
    }

    private void list(List<String> tokens) {
        int limit = tokens.size() >= 2 ? parseInt(tokens.get(1), "limit") : DEFAULT_LIMIT;
        VacancyFilter filter = new VacancyFilter();
        printer.printVacancies(vacancyService.find(filter, SortField.DATE, limit));
    }

    private void search(List<String> tokens) {
        require(tokens.size() >= 2, "Формат: search <keyword> [limit]");
        VacancyFilter filter = new VacancyFilter();
        filter.setKeyword(tokens.get(1));
        int limit = tokens.size() >= 3 ? parseInt(tokens.get(2), "limit") : DEFAULT_LIMIT;
        printer.printVacancies(vacancyService.find(filter, SortField.DATE, limit));
    }

    private void filter(List<String> tokens) {
        Map<String, String> options = options(tokens, 1);
        VacancyFilter filter = filterFromOptions(options);
        SortField sort = SortField.fromString(options.get("sort"));
        int limit = parseInt(options.getOrDefault("limit", String.valueOf(DEFAULT_LIMIT)), "limit");
        printer.printVacancies(vacancyService.find(filter, sort, limit));
    }

    private void view(List<String> tokens) {
        require(tokens.size() >= 2, "Формат: view <id>");
        Optional<Vacancy> vacancy = vacancyService.findById(parseLong(tokens.get(1), "id"));
        vacancy.ifPresentOrElse(printer::printVacancy, () -> System.out.println("Вакансия не найдена."));
    }

    private void addAlert(List<String> tokens) {
        Map<String, String> options = options(tokens, 1);
        require(options.containsKey("name"), "Для уведомления нужен name=<название>");
        AlertRule rule = new AlertRule();
        rule.setName(options.get("name"));
        rule.setCity(options.get("city"));
        rule.setCompany(options.get("company"));
        rule.setKeyword(options.get("keyword"));
        if (options.containsKey("minsalary")) {
            rule.setMinSalary(parseInt(options.get("minsalary"), "minSalary"));
        }
        if (options.containsKey("employment")) {
            rule.setEmploymentType(EmploymentType.fromText(options.get("employment")));
        }
        AlertRule saved = alertService.add(rule);
        System.out.println("Уведомление добавлено: id=" + saved.getId());
    }

    private void export(List<String> tokens) {
        require(tokens.size() >= 3, "Формат: export <csv|json|html> <file> [limit]");
        String format = tokens.get(1);
        Path file = Path.of(tokens.get(2));
        int limit = tokens.size() >= 4 ? parseInt(tokens.get(3), "limit") : 10_000;
        VacancyFilter filter = new VacancyFilter();
        List<Vacancy> vacancies = vacancyService.find(filter, SortField.DATE, limit);
        exportService.export(format, vacancies, file);
        System.out.println("Экспортировано вакансий: " + vacancies.size() + ". Файл: " + file.toAbsolutePath());
    }

    private void stats() {
        printer.printStats(
                vacancyService.countActive(),
                analyticsService.countByCategory(),
                analyticsService.countByCity(),
                analyticsService.salaryByCategory(),
                analyticsService.salaryByCity()
        );
    }

    private void history(List<String> tokens) {
        int limit = tokens.size() >= 2 ? parseInt(tokens.get(1), "limit") : DEFAULT_LIMIT;
        printer.printHistory(historyService.latest(limit));
    }

    private VacancyFilter filterFromOptions(Map<String, String> options) {
        VacancyFilter filter = new VacancyFilter();
        filter.setCity(options.get("city"));
        filter.setCompany(options.get("company"));
        filter.setKeyword(options.get("keyword"));
        if (options.containsKey("minsalary")) {
            filter.setMinSalary(parseInt(options.get("minsalary"), "minSalary"));
        }
        if (options.containsKey("salary>")) {
            filter.setMinSalary(parseInt(options.get("salary>"), "salary>"));
        }
        if (options.containsKey("salary>=")) {
            filter.setMinSalary(parseInt(options.get("salary>="), "salary>="));
        }
        if (options.containsKey("employment")) {
            filter.setEmploymentType(EmploymentType.fromText(options.get("employment")));
        }
        return filter;
    }

    private Map<String, String> options(List<String> tokens, int fromIndex) {
        Map<String, String> options = new HashMap<>();
        for (int i = fromIndex; i < tokens.size(); i++) {
            String token = tokens.get(i);
            int index = token.indexOf('=');
            if (index <= 0) {
                throw new IllegalArgumentException("Ожидался параметр key=value, получено: " + token);
            }
            String key = token.substring(0, index).trim().toLowerCase().replace("_", "");
            String value = token.substring(index + 1).trim();
            options.put(key, value);
        }
        return options;
    }

    private int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + fieldName + " должен быть числом");
        }
    }

    private long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр " + fieldName + " должен быть числом");
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
