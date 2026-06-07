package matve.vacancies.repository;

import matve.vacancies.domain.CountStat;
import matve.vacancies.domain.SalaryStats;

import java.util.List;

public interface AnalyticsRepository {
    List<CountStat> countByCategory();
    List<CountStat> countByCity();
    List<SalaryStats> salaryByCategory();
    List<SalaryStats> salaryByCity();
}
