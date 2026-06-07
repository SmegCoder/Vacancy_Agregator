package matve.vacancies.parser;

import org.junit.jupiter.api.Test;
import matve.vacancies.domain.EmploymentType;
import matve.vacancies.domain.Source;
import matve.vacancies.domain.SourceType;
import matve.vacancies.domain.Vacancy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeadHunterParserTest {
    @Test
    void parsesVacanciesFromHtml() throws Exception {
        String html = Files.readString(Path.of("src/test/resources/fixtures/hh.html"));
        Source source = new Source(1L, "hh", SourceType.HEADHUNTER, "https://hh.ru/search/vacancy?page={page}", true, 1);

        List<Vacancy> vacancies = new HeadHunterParser().parse(source, html, "https://hh.ru/search/vacancy?page=0");

        assertEquals(2, vacancies.size());
        Vacancy first = vacancies.get(0);
        assertEquals("Java Backend Developer", first.getTitle());
        assertEquals("ООО Ромашка", first.getCompany());
        assertEquals("Москва", first.getCity());
        assertEquals(150000, first.getSalaryMin().intValue());
        assertNull(first.getSalaryMax());
        assertEquals("Backend", first.getCategory());
        assertEquals(EmploymentType.UNKNOWN, first.getEmploymentType());
    }
}
