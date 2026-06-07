package matve.vacancies;

import matve.vacancies.cli.ConsoleApplication;
import matve.vacancies.cli.ConsolePrinter;
import matve.vacancies.export.CsvVacancyExporter;
import matve.vacancies.export.ExportService;
import matve.vacancies.export.HtmlVacancyExporter;
import matve.vacancies.export.JsonVacancyExporter;
import matve.vacancies.parser.JsoupHtmlFetcher;
import matve.vacancies.parser.ParserFactory;
import matve.vacancies.repository.*;
import matve.vacancies.service.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Database database = Database.defaultDatabase();
        database.initialize();

        SourceRepository sourceRepository = new JdbcSourceRepository(database);
        VacancyRepository vacancyRepository = new JdbcVacancyRepository(database);
        AlertRuleRepository alertRuleRepository = new JdbcAlertRuleRepository(database);
        ChangeEventRepository changeEventRepository = new JdbcChangeEventRepository(database);
        AnalyticsRepository analyticsRepository = new JdbcAnalyticsRepository(database);

        SourceService sourceService = new SourceService(sourceRepository);
        VacancyService vacancyService = new VacancyService(vacancyRepository);
        AlertService alertService = new AlertService(alertRuleRepository);
        AnalyticsService analyticsService = new AnalyticsService(analyticsRepository);
        HistoryService historyService = new HistoryService(changeEventRepository);
        AggregationService aggregationService = new AggregationService(
                sourceRepository,
                vacancyRepository,
                changeEventRepository,
                new JsoupHtmlFetcher(),
                new ParserFactory()
        );
        AutoRefreshService autoRefreshService = new AutoRefreshService(aggregationService, alertService);
        ExportService exportService = new ExportService(List.of(
                new CsvVacancyExporter(),
                new JsonVacancyExporter(),
                new HtmlVacancyExporter()
        ));

        ConsoleApplication application = new ConsoleApplication(
                sourceService,
                vacancyService,
                aggregationService,
                alertService,
                analyticsService,
                historyService,
                autoRefreshService,
                exportService,
                new ConsolePrinter()
        );
        application.run();
    }
}
