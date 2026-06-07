package matve.vacancies.service;

import matve.vacancies.domain.SortField;
import matve.vacancies.domain.Vacancy;
import matve.vacancies.domain.VacancyFilter;
import matve.vacancies.repository.VacancyRepository;

import java.util.List;
import java.util.Optional;

public class VacancyService {
    private final VacancyRepository vacancyRepository;

    public VacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    public List<Vacancy> find(VacancyFilter filter, SortField sortField, int limit) {
        return vacancyRepository.find(filter, sortField, limit);
    }

    public Optional<Vacancy> findById(long id) {
        return vacancyRepository.findById(id);
    }

    public long countActive() {
        return vacancyRepository.countActive();
    }
}
