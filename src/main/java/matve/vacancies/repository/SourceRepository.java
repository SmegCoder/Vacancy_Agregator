package matve.vacancies.repository;

import matve.vacancies.domain.Source;

import java.util.List;
import java.util.Optional;

public interface SourceRepository {
    Source save(Source source);
    List<Source> findAll();
    List<Source> findEnabled();
    Optional<Source> findById(long id);
    void setEnabled(long id, boolean enabled);
    void delete(long id);
}
