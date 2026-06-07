package matve.vacancies.repository;

import matve.vacancies.domain.SortField;
import matve.vacancies.domain.Vacancy;
import matve.vacancies.domain.VacancyFilter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VacancyRepository {
    UpsertResult upsert(Vacancy vacancy);
    Optional<Vacancy> findById(long id);
    List<Vacancy> find(VacancyFilter filter, SortField sortField, int limit);
    List<Vacancy> deactivateMissing(long sourceId, Set<String> actualUrls);
    long countActive();
}
