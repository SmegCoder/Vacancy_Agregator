package matve.vacancies.parser;

import matve.vacancies.domain.Source;
import matve.vacancies.domain.Vacancy;

import java.util.List;

public interface VacancyParser {
    List<Vacancy> parse(Source source, String html, String pageUrl);
}
