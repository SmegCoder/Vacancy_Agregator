package matve.vacancies.export;

import matve.vacancies.domain.Vacancy;

import java.nio.file.Path;
import java.util.List;

public interface VacancyExporter {
    void export(List<Vacancy> vacancies, Path file);
    String format();
}
