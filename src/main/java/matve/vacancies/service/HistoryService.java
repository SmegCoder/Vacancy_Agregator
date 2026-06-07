package matve.vacancies.service;

import matve.vacancies.domain.ChangeEvent;
import matve.vacancies.repository.ChangeEventRepository;

import java.util.List;

public class HistoryService {
    private final ChangeEventRepository changeEventRepository;

    public HistoryService(ChangeEventRepository changeEventRepository) {
        this.changeEventRepository = changeEventRepository;
    }

    public List<ChangeEvent> latest(int limit) {
        return changeEventRepository.latest(limit);
    }
}
