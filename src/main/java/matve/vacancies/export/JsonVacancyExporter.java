package matve.vacancies.export;

import matve.vacancies.domain.Vacancy;
import matve.vacancies.util.TextUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JsonVacancyExporter implements VacancyExporter {
    @Override
    public void export(List<Vacancy> vacancies, Path file) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            StringBuilder builder = new StringBuilder();
            builder.append("[\n");
            for (int i = 0; i < vacancies.size(); i++) {
                Vacancy v = vacancies.get(i);
                builder.append("  {\n")
                        .append("    \"id\": ").append(v.getId()).append(",\n")
                        .append("    \"title\": \"").append(json(v.getTitle())).append("\",\n")
                        .append("    \"company\": \"").append(json(v.getCompany())).append("\",\n")
                        .append("    \"city\": \"").append(json(v.getCity())).append("\",\n")
                        .append("    \"salaryMin\": ").append(number(v.getSalaryMin())).append(",\n")
                        .append("    \"salaryMax\": ").append(number(v.getSalaryMax())).append(",\n")
                        .append("    \"currency\": \"").append(json(v.getCurrency())).append("\",\n")
                        .append("    \"employmentType\": \"").append(v.getEmploymentType()).append("\",\n")
                        .append("    \"category\": \"").append(json(v.getCategory())).append("\",\n")
                        .append("    \"publishedDate\": \"").append(json(String.valueOf(v.getPublishedDate()))).append("\",\n")
                        .append("    \"url\": \"").append(json(v.getUrl())).append("\"\n")
                        .append("  }");
                if (i + 1 < vacancies.size()) {
                    builder.append(',');
                }
                builder.append('\n');
            }
            builder.append("]\n");
            Files.writeString(file, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExportException("Cannot export JSON", e);
        }
    }

    @Override
    public String format() {
        return "json";
    }

    private String json(String value) {
        return TextUtils.safe(value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String number(Integer value) {
        return value == null ? "null" : value.toString();
    }
}
