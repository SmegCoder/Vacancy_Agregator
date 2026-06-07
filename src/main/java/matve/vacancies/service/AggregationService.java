package matve.vacancies.service;

import matve.vacancies.domain.ChangeEvent;
import matve.vacancies.domain.Source;
import matve.vacancies.domain.Vacancy;
import matve.vacancies.parser.HtmlFetcher;
import matve.vacancies.parser.ParserFactory;
import matve.vacancies.parser.VacancyParser;
import matve.vacancies.repository.ChangeEventRepository;
import matve.vacancies.repository.SourceRepository;
import matve.vacancies.repository.UpsertResult;
import matve.vacancies.repository.VacancyRepository;
import matve.vacancies.util.TextUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AggregationService {
    private final SourceRepository sourceRepository;
    private final VacancyRepository vacancyRepository;
    private final ChangeEventRepository changeEventRepository;
    private final HtmlFetcher htmlFetcher;
    private final ParserFactory parserFactory;

    public AggregationService(SourceRepository sourceRepository,
                              VacancyRepository vacancyRepository,
                              ChangeEventRepository changeEventRepository,
                              HtmlFetcher htmlFetcher,
                              ParserFactory parserFactory) {
        this.sourceRepository = sourceRepository;
        this.vacancyRepository = vacancyRepository;
        this.changeEventRepository = changeEventRepository;
        this.htmlFetcher = htmlFetcher;
        this.parserFactory = parserFactory;
    }

    public AggregationResult refreshAll() {
        AggregationResult result = new AggregationResult();
        List<Source> sources = sourceRepository.findEnabled();
        for (Source source : sources) {
            refreshSource(source, result);
        }
        return result;
    }

    private void refreshSource(Source source, AggregationResult result) {
        Set<String> actualUrls = new HashSet<>();
        boolean sourceLoaded = false;
        for (int page = 0; page < source.getMaxPages(); page++) {
            String pageUrl = source.pageUrl(page);
            try {
                String html = htmlFetcher.fetch(pageUrl);
                VacancyParser parser = parserFactory.get(source.getType());
                Map<String, Vacancy> unique = uniqueByUrl(parser.parse(source, html, pageUrl));
                for (Vacancy vacancy : unique.values()) {
                    enrich(source, vacancy);
                    actualUrls.add(vacancy.getUrl());
                    UpsertResult upsert = vacancyRepository.upsert(vacancy);
                    result.incrementFetched();
                    if (upsert.isInserted()) {
                        result.getAdded().add(upsert.getVacancy());
                        changeEventRepository.save(new ChangeEvent(upsert.getVacancy().getId(), "ADDED", "Добавлена вакансия: " + upsert.getVacancy().getTitle()));
                    } else if (upsert.isUpdated()) {
                        result.getUpdated().add(upsert.getVacancy());
                        changeEventRepository.save(new ChangeEvent(upsert.getVacancy().getId(), "UPDATED", "Обновлена вакансия: " + upsert.getVacancy().getTitle()));
                    }
                }
                sourceLoaded = true;
            } catch (RuntimeException e) {
                result.getErrors().add(source.getName() + ": " + e.getMessage());
                changeEventRepository.save(new ChangeEvent(null, "ERROR", "Ошибка обновления источника " + source.getName() + ": " + e.getMessage()));
            }
        }
        if (sourceLoaded) {
            List<Vacancy> removed = vacancyRepository.deactivateMissing(source.getId(), actualUrls);
            result.getRemoved().addAll(removed);
            for (Vacancy vacancy : removed) {
                changeEventRepository.save(new ChangeEvent(vacancy.getId(), "REMOVED", "Вакансия стала неактуальной: " + vacancy.getTitle()));
            }
        }
    }

    private Map<String, Vacancy> uniqueByUrl(List<Vacancy> vacancies) {
        Map<String, Vacancy> unique = new LinkedHashMap<>();
        for (Vacancy vacancy : vacancies) {
            if (vacancy.getUrl() != null && !vacancy.getUrl().isBlank()) {
                unique.putIfAbsent(vacancy.getUrl(), vacancy);
            }
        }
        return unique;
    }

    private void enrich(Source source, Vacancy vacancy) {
        vacancy.setSourceId(source.getId());
        vacancy.setSourceName(source.getName());
        vacancy.setActive(true);
        vacancy.setContentHash(TextUtils.hash(String.join("|",
                TextUtils.safe(vacancy.getTitle()),
                TextUtils.safe(vacancy.getCompany()),
                TextUtils.safe(vacancy.getCity()),
                String.valueOf(vacancy.getSalaryMin()),
                String.valueOf(vacancy.getSalaryMax()),
                TextUtils.safe(vacancy.getCurrency()),
                TextUtils.safe(vacancy.getDescription()),
                TextUtils.safe(vacancy.getRequirements()),
                String.valueOf(vacancy.getEmploymentType()),
                TextUtils.safe(vacancy.getCategory()),
                String.valueOf(vacancy.getPublishedDate())
        )));
    }
}
