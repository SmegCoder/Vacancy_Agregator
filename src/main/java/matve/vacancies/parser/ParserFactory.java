package matve.vacancies.parser;

import matve.vacancies.domain.SourceType;

public class ParserFactory {
    private final VacancyParser headHunterParser;
    private final VacancyParser habrCareerParser;

    public ParserFactory() {
        this(new HeadHunterParser(), new HabrCareerParser());
    }

    public ParserFactory(VacancyParser headHunterParser, VacancyParser habrCareerParser) {
        this.headHunterParser = headHunterParser;
        this.habrCareerParser = habrCareerParser;
    }

    public VacancyParser get(SourceType type) {
        return switch (type) {
            case HEADHUNTER -> headHunterParser;
            case HABR_CAREER -> habrCareerParser;
        };
    }
}
