package matve.vacancies.export;

import matve.vacancies.domain.Vacancy;
import matve.vacancies.util.TextUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvVacancyExporter implements VacancyExporter {
    @Override
    public void export(List<Vacancy> vacancies, Path file) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            StringBuilder builder = new StringBuilder();
            builder.append("id,title,company,city,salary_min,salary_max,currency,employment_type,category,published_date,url\n");
            for (Vacancy vacancy : vacancies) {
                builder.append(csv(String.valueOf(vacancy.getId()))).append(',')
                        .append(csv(vacancy.getTitle())).append(',')
                        .append(csv(vacancy.getCompany())).append(',')
                        .append(csv(vacancy.getCity())).append(',')
                        .append(csv(String.valueOf(vacancy.getSalaryMin()))).append(',')
                        .append(csv(String.valueOf(vacancy.getSalaryMax()))).append(',')
                        .append(csv(vacancy.getCurrency())).append(',')
                        .append(csv(String.valueOf(vacancy.getEmploymentType()))).append(',')
                        .append(csv(vacancy.getCategory())).append(',')
                        .append(csv(String.valueOf(vacancy.getPublishedDate()))).append(',')
                        .append(csv(vacancy.getUrl())).append('\n');
            }
            Files.writeString(file, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExportException("Cannot export CSV", e);
        }
    }

    @Override
    public String format() {
        return "csv";
    }

    private String csv(String value) {
        String safe = TextUtils.safe(value).replace("null", "");
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
