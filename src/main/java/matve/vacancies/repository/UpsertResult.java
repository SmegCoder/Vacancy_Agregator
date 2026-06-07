package matve.vacancies.repository;

import matve.vacancies.domain.Vacancy;

public class UpsertResult {
    private final Vacancy vacancy;
    private final boolean inserted;
    private final boolean updated;

    public UpsertResult(Vacancy vacancy, boolean inserted, boolean updated) {
        this.vacancy = vacancy;
        this.inserted = inserted;
        this.updated = updated;
    }

    public Vacancy getVacancy() { return vacancy; }
    public boolean isInserted() { return inserted; }
    public boolean isUpdated() { return updated; }
}
