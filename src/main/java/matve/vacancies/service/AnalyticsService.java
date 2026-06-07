package matve.vacancies.service;

import matve.vacancies.domain.CountStat;
import matve.vacancies.domain.SalaryStats;
import matve.vacancies.repository.AnalyticsRepository;

import java.util.List;

public class AnalyticsService {
    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public List<CountStat> countByCategory() {
        return analyticsRepository.countByCategory();
    }

    public List<CountStat> countByCity() {
        return analyticsRepository.countByCity();
    }

    public List<SalaryStats> salaryByCategory() {
        return analyticsRepository.salaryByCategory();
    }

    public List<SalaryStats> salaryByCity() {
        return analyticsRepository.salaryByCity();
    }
}
