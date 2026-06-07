package matve.vacancies.export;

import matve.vacancies.domain.Vacancy;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportService {
    private final Map<String, VacancyExporter> exporters = new HashMap<>();

    public ExportService(List<VacancyExporter> exporters) {
        for (VacancyExporter exporter : exporters) {
            this.exporters.put(exporter.format().toLowerCase(), exporter);
        }
    }

    public void export(String format, List<Vacancy> vacancies, Path file) {
        VacancyExporter exporter = exporters.get(format.toLowerCase());
        if (exporter == null) {
            throw new IllegalArgumentException("Unknown export format: " + format + ". Allowed: csv, json, html");
        }
        exporter.export(vacancies, file);
    }
}
