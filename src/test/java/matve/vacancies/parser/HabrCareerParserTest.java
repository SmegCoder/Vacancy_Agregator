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

class HabrCareerParserTest {
    @Test
    void parsesHabrCareerVacancy() throws Exception {
        String html = Files.readString(Path.of("src/test/resources/fixtures/habr.html"));
        Source source = new Source(1L, "habr", SourceType.HABR_CAREER, "https://career.habr.com/vacancies?page={page}", true, 1);

        List<Vacancy> vacancies = new HabrCareerParser().parse(source, html, "https://career.habr.com/vacancies?page=0");

        assertEquals(1, vacancies.size());
        Vacancy vacancy = vacancies.get(0);
        assertEquals("Python ML Engineer", vacancy.getTitle());
        assertEquals("DataLab", vacancy.getCompany());
        assertEquals(250000, vacancy.getSalaryMax().intValue());
        assertEquals("Data/ML", vacancy.getCategory());
        assertEquals(EmploymentType.REMOTE, vacancy.getEmploymentType());
    }
}
