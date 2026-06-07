package matve.vacancies.service;

import matve.vacancies.domain.Source;
import matve.vacancies.domain.SourceType;
import matve.vacancies.repository.SourceRepository;

import java.util.List;

public class SourceService {
    private final SourceRepository sourceRepository;

    public SourceService(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    public Source add(String name, SourceType type, String urlTemplate, int maxPages) {
        Source source = new Source();
        source.setName(name);
        source.setType(type);
        source.setUrlTemplate(urlTemplate);
        source.setMaxPages(maxPages);
        source.setEnabled(true);
        return sourceRepository.save(source);
    }

    public List<Source> all() {
        return sourceRepository.findAll();
    }

    public void enable(long id) {
        sourceRepository.setEnabled(id, true);
    }

    public void disable(long id) {
        sourceRepository.setEnabled(id, false);
    }

    public void delete(long id) {
        sourceRepository.delete(id);
    }
}
