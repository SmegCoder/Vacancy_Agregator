package matve.vacancies.service;

import matve.vacancies.domain.AlertRule;
import matve.vacancies.domain.EmploymentType;
import matve.vacancies.domain.Vacancy;
import matve.vacancies.repository.AlertRuleRepository;
import matve.vacancies.util.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlertService {
    private final AlertRuleRepository alertRuleRepository;

    public AlertService(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    public AlertRule add(AlertRule rule) {
        return alertRuleRepository.save(rule);
    }

    public List<AlertRule> all() {
        return alertRuleRepository.findAll();
    }

    public List<String> matchNewVacancies(List<Vacancy> newVacancies) {
        List<AlertRule> rules = alertRuleRepository.findEnabled();
        List<String> notifications = new ArrayList<>();
        for (AlertRule rule : rules) {
            for (Vacancy vacancy : newVacancies) {
                if (matches(rule, vacancy)) {
                    notifications.add("[" + rule.getName() + "] " + vacancy.getTitle() + " — " + vacancy.getCompany() + " — " + vacancy.shortSalary());
                }
            }
        }
        return notifications;
    }

    private boolean matches(AlertRule rule, Vacancy vacancy) {
        if (rule.getCity() != null && !contains(vacancy.getCity(), rule.getCity())) {
            return false;
        }
        if (rule.getCompany() != null && !contains(vacancy.getCompany(), rule.getCompany())) {
            return false;
        }
        if (rule.getMinSalary() != null && vacancy.averageSalary() < rule.getMinSalary()) {
            return false;
        }
        if (rule.getKeyword() != null && !TextUtils.containsIgnoreCase(vacancy.searchableText(), rule.getKeyword())) {
            return false;
        }
        return rule.getEmploymentType() == null
                || rule.getEmploymentType() == EmploymentType.UNKNOWN
                || rule.getEmploymentType() == vacancy.getEmploymentType();
    }

    private boolean contains(String text, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        if (text == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }
}
