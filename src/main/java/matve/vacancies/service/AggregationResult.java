package matve.vacancies.service;

import matve.vacancies.domain.Vacancy;

import java.util.ArrayList;
import java.util.List;

public class AggregationResult {
    private int fetched;
    private final List<Vacancy> added = new ArrayList<>();
    private final List<Vacancy> updated = new ArrayList<>();
    private final List<Vacancy> removed = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public int getFetched() { return fetched; }
    public void incrementFetched() { fetched++; }

    public List<Vacancy> getAdded() { return added; }
    public List<Vacancy> getUpdated() { return updated; }
    public List<Vacancy> getRemoved() { return removed; }
    public List<String> getErrors() { return errors; }

    public int totalChanged() {
        return added.size() + updated.size() + removed.size();
    }
}
