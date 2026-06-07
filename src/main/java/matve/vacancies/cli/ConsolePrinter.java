package matve.vacancies.cli;

import matve.vacancies.domain.*;
import matve.vacancies.service.AggregationResult;
import matve.vacancies.util.TextUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsolePrinter {
    public void printHelp() {
        System.out.println("""
                Команды:
                  help                                      показать справку
                  sources                                   список источников
                  add-source <name> <type> <url> [pages]    добавить источник, type: HEADHUNTER или HABR_CAREER
                  enable-source <id>                        включить источник
                  disable-source <id>                       выключить источник
                  delete-source <id>                        удалить источник
                  refresh                                   вручную обновить вакансии
                  auto-refresh <seconds>                    включить автообновление, минимум 10 секунд
                  stop-auto-refresh                         выключить автообновление
                  list [limit]                              показать последние вакансии
                  search <keyword> [limit]                  поиск по названию/описанию/требованиям
                  filter key=value ...                      фильтр: city, company, minSalary, employment, keyword, sort, limit
                  view <id>                                 полное описание вакансии
                  add-alert key=value ...                   уведомление: name, city, company, minSalary, employment, keyword
                  alerts                                    список уведомлений
                  export <csv|json|html> <file> [limit]     экспорт вакансий
                  stats                                     статистика и аналитика
                  history [limit]                           история изменений
                  exit                                      выход

                Примеры:
                  add-source hh HEADHUNTER "https://hh.ru/search/vacancy?text=java&page={page}" 2
                  filter city=Москва minSalary=120000 keyword=Java sort=salary limit=20
                  add-alert name=java keyword=Java city=Москва minSalary=100000
                """);
    }

    public void printSources(List<Source> sources) {
        if (sources.isEmpty()) {
            System.out.println("Источников пока нет. Добавьте источник командой add-source.");
            return;
        }
        System.out.printf("%-4s %-18s %-14s %-8s %-5s %s%n", "ID", "NAME", "TYPE", "ENABLED", "PAGES", "URL");
        for (Source source : sources) {
            System.out.printf("%-4d %-18s %-14s %-8s %-5d %s%n",
                    source.getId(), source.getName(), source.getType(), source.isEnabled(), source.getMaxPages(), source.getUrlTemplate());
        }
    }

    public void printVacancies(List<Vacancy> vacancies) {
        if (vacancies.isEmpty()) {
            System.out.println("Вакансии не найдены.");
            return;
        }
        System.out.printf("%-5s %-38s %-22s %-16s %-18s %-10s%n", "ID", "TITLE", "COMPANY", "CITY", "SALARY", "CATEGORY");
        for (Vacancy vacancy : vacancies) {
            System.out.printf("%-5d %-38s %-22s %-16s %-18s %-10s%n",
                    vacancy.getId(),
                    TextUtils.shorten(vacancy.getTitle(), 37),
                    TextUtils.shorten(vacancy.getCompany(), 21),
                    TextUtils.shorten(vacancy.getCity(), 15),
                    TextUtils.shorten(vacancy.shortSalary(), 17),
                    TextUtils.shorten(vacancy.getCategory(), 10));
        }
    }

    public void printVacancy(Vacancy vacancy) {
        System.out.println("ID: " + vacancy.getId());
        System.out.println("Название: " + vacancy.getTitle());
        System.out.println("Компания: " + vacancy.getCompany());
        System.out.println("Город: " + vacancy.getCity());
        System.out.println("Зарплата: " + vacancy.shortSalary());
        System.out.println("Тип занятости: " + vacancy.getEmploymentType());
        System.out.println("Категория: " + vacancy.getCategory());
        System.out.println("Дата публикации: " + vacancy.getPublishedDate());
        System.out.println("Источник: " + vacancy.getSourceName());
        System.out.println("Ссылка: " + vacancy.getUrl());
        System.out.println("Описание: " + TextUtils.safe(vacancy.getDescription()));
        System.out.println("Требования: " + TextUtils.safe(vacancy.getRequirements()));
    }

    public void printAggregation(AggregationResult result, List<String> notifications) {
        System.out.println("Обновление завершено.");
        System.out.println("Загружено карточек: " + result.getFetched());
        System.out.println("Новых: " + result.getAdded().size());
        System.out.println("Обновлено: " + result.getUpdated().size());
        System.out.println("Удалено/устарело: " + result.getRemoved().size());
        if (!result.getErrors().isEmpty()) {
            System.out.println("Ошибки:");
            for (String error : result.getErrors()) {
                System.out.println("  - " + error);
            }
        }
        if (!notifications.isEmpty()) {
            System.out.println("Уведомления:");
            for (String notification : notifications) {
                System.out.println("  - " + notification);
            }
        }
    }

    public void printAlerts(List<AlertRule> rules) {
        if (rules.isEmpty()) {
            System.out.println("Правил уведомлений пока нет.");
            return;
        }
        System.out.printf("%-4s %-16s %-14s %-18s %-10s %-12s %s%n", "ID", "NAME", "CITY", "COMPANY", "SALARY", "EMPLOYMENT", "KEYWORD");
        for (AlertRule rule : rules) {
            System.out.printf("%-4d %-16s %-14s %-18s %-10s %-12s %s%n",
                    rule.getId(), rule.getName(), TextUtils.safe(rule.getCity()), TextUtils.safe(rule.getCompany()),
                    rule.getMinSalary() == null ? "" : rule.getMinSalary(), rule.getEmploymentType(), TextUtils.safe(rule.getKeyword()));
        }
    }

    public void printStats(long activeCount, List<CountStat> byCategory, List<CountStat> byCity,
                           List<SalaryStats> salaryByCategory, List<SalaryStats> salaryByCity) {
        System.out.println("Активных вакансий: " + activeCount);
        System.out.println("\nКоличество по категориям:");
        printCountStats(byCategory);
        System.out.println("\nКоличество по городам:");
        printCountStats(byCity);
        System.out.println("\nЗарплаты по категориям:");
        printSalaryStats(salaryByCategory);
        System.out.println("\nЗарплаты по городам:");
        printSalaryStats(salaryByCity);
    }

    public void printHistory(List<ChangeEvent> events) {
        if (events.isEmpty()) {
            System.out.println("История пуста.");
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (ChangeEvent event : events) {
            System.out.println(event.getCreatedAt().format(formatter) + " [" + event.getType() + "] " + event.getMessage());
        }
    }

    private void printCountStats(List<CountStat> stats) {
        if (stats.isEmpty()) {
            System.out.println("  нет данных");
            return;
        }
        for (CountStat stat : stats) {
            System.out.printf("  %-20s %d%n", stat.getName(), stat.getCount());
        }
    }

    private void printSalaryStats(List<SalaryStats> stats) {
        if (stats.isEmpty()) {
            System.out.println("  нет данных");
            return;
        }
        System.out.printf("  %-20s %-7s %-10s %-10s %-10s%n", "Группа", "Кол-во", "Мин", "Средняя", "Макс");
        for (SalaryStats stat : stats) {
            System.out.printf("  %-20s %-7d %-10d %-10d %-10d%n",
                    stat.getGroupName(), stat.getCount(), stat.getMinSalary(), stat.getAverageSalary(), stat.getMaxSalary());
        }
    }
}
