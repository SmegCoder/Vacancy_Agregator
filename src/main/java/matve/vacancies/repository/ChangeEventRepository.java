package matve.vacancies.repository;

import matve.vacancies.domain.ChangeEvent;

import java.util.List;

public interface ChangeEventRepository {
    ChangeEvent save(ChangeEvent event);
    List<ChangeEvent> latest(int limit);
}
