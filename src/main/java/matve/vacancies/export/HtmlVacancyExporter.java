package matve.vacancies.export;

import matve.vacancies.domain.Vacancy;
import matve.vacancies.util.TextUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HtmlVacancyExporter implements VacancyExporter {
    @Override
    public void export(List<Vacancy> vacancies, Path file) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            StringBuilder builder = new StringBuilder();
            builder.append("<!doctype html><html lang=\"ru\"><head><meta charset=\"UTF-8\"><title>Vacancies</title>")
                    .append("<style>body{font-family:Arial,sans-serif;margin:24px}table{border-collapse:collapse;width:100%}td,th{border:1px solid #ddd;padding:8px}th{background:#eee}</style>")
                    .append("</head><body><h1>Вакансии</h1><table>")
                    .append("<tr><th>ID</th><th>Название</th><th>Компания</th><th>Город</th><th>Зарплата</th><th>Категория</th><th>Ссылка</th></tr>");
            for (Vacancy v : vacancies) {
                builder.append("<tr>")
                        .append("<td>").append(v.getId()).append("</td>")
                        .append("<td>").append(html(v.getTitle())).append("</td>")
                        .append("<td>").append(html(v.getCompany())).append("</td>")
                        .append("<td>").append(html(v.getCity())).append("</td>")
                        .append("<td>").append(html(v.shortSalary())).append("</td>")
                        .append("<td>").append(html(v.getCategory())).append("</td>")
                        .append("<td><a href=\"").append(html(v.getUrl())).append("\">открыть</a></td>")
                        .append("</tr>");
            }
            builder.append("</table></body></html>");
            Files.writeString(file, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExportException("Cannot export HTML", e);
        }
    }

    @Override
    public String format() {
        return "html";
    }

    private String html(String value) {
        return TextUtils.safe(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
