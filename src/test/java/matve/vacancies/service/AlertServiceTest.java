package matve.vacancies.service;

import org.junit.jupiter.api.Test;
import matve.vacancies.domain.AlertRule;
import matve.vacancies.domain.Vacancy;
import matve.vacancies.repository.AlertRuleRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertServiceTest {
    @Test
    void matchesNewVacanciesByKeywordCityAndSalary() {
        AlertRule rule = new AlertRule();
        rule.setId(1L);
        rule.setName("java-msk");
        rule.setKeyword("java");
        rule.setCity("Москва");
        rule.setMinSalary(100000);

        Vacancy vacancy = new Vacancy();
        vacancy.setTitle("Java Developer");
        vacancy.setCompany("TestCompany");
        vacancy.setCity("Москва");
        vacancy.setSalaryMin(150000);
        vacancy.setDescription("Backend development");

        AlertService service = new AlertService(new FakeAlertRuleRepository(List.of(rule)));

        assertEquals(1, service.matchNewVacancies(List.of(vacancy)).size());
    }

    private record FakeAlertRuleRepository(List<AlertRule> rules) implements AlertRuleRepository {
        @Override
        public AlertRule save(AlertRule rule) {
            return rule;
        }

        @Override
        public List<AlertRule> findAll() {
            return rules;
        }

        @Override
        public List<AlertRule> findEnabled() {
            return rules;
        }
    }
}
