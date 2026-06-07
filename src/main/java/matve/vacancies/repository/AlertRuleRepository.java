package matve.vacancies.repository;

import matve.vacancies.domain.AlertRule;

import java.util.List;

public interface AlertRuleRepository {
    AlertRule save(AlertRule rule);
    List<AlertRule> findAll();
    List<AlertRule> findEnabled();
}
