package matve.vacancies.repository;

import org.junit.jupiter.api.Test;
import matve.vacancies.domain.SortField;
import matve.vacancies.domain.Vacancy;
import matve.vacancies.domain.VacancyFilter;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcVacancyRepositoryTest {
    @Test
    void insertsAndFindsVacancy() {
        Database database = new Database("jdbc:h2:mem:testdb" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        database.initialize();
        JdbcVacancyRepository repository = new JdbcVacancyRepository(database);

        Vacancy vacancy = new Vacancy();
        vacancy.setSourceId(1L);
        vacancy.setSourceName("test");
        vacancy.setExternalId("1");
        vacancy.setTitle("Java Developer");
        vacancy.setCompany("Company");
        vacancy.setCity("Москва");
        vacancy.setSalaryMin(120000);
        vacancy.setDescription("Java backend");
        vacancy.setRequirements("SQL");
        vacancy.setCategory("Backend");
        vacancy.setPublishedDate(LocalDate.of(2026, 6, 1));
        vacancy.setUrl("https://example.com/vacancy/1");
        vacancy.setContentHash("hash1");

        UpsertResult result = repository.upsert(vacancy);

        assertTrue(result.isInserted());
        VacancyFilter filter = new VacancyFilter();
        filter.setCity("Москва");
        List<Vacancy> found = repository.find(filter, SortField.DATE, 10);
        assertEquals(1, found.size());
        assertEquals("Java Developer", found.get(0).getTitle());
    }
}
