package matve.vacancies.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import matve.vacancies.domain.EmploymentType;
import matve.vacancies.domain.Source;
import matve.vacancies.domain.Vacancy;
import matve.vacancies.util.CategoryClassifier;
import matve.vacancies.util.SalaryParser;
import matve.vacancies.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class HabrCareerParser implements VacancyParser {
    @Override
    public List<Vacancy> parse(Source source, String html, String pageUrl) {
        Document document = Jsoup.parse(html, pageUrl);
        Elements cards = document.select(".vacancy-card, .job-card, [data-test-id=vacancy-card]");
        List<Vacancy> vacancies = new ArrayList<>();
        for (Element card : cards) {
            Vacancy vacancy = parseCard(card, pageUrl);
            if (vacancy != null) {
                vacancies.add(vacancy);
            }
        }
        return vacancies;
    }

    private Vacancy parseCard(Element card, String pageUrl) {
        String title = ParserUtils.firstText(card,
                ".vacancy-card__title a",
                ".job-card__title a",
                "a[href*='/vacancies/']",
                "a[href*='/jobs/']"
        );
        String href = ParserUtils.firstAttr(card, "href",
                ".vacancy-card__title a",
                ".job-card__title a",
                "a[href*='/vacancies/']",
                "a[href*='/jobs/']"
        );
        String url = ParserUtils.normalizeUrl(pageUrl, href);
        if (title.isBlank() || url.isBlank()) {
            return null;
        }

        String company = ParserUtils.firstText(card,
                ".vacancy-card__company-title",
                ".job-card__company-title",
                ".company"
        );
        String city = ParserUtils.firstText(card,
                ".vacancy-card__meta",
                ".job-card__meta",
                ".city"
        );
        String salaryText = ParserUtils.firstText(card,
                ".vacancy-card__salary",
                ".job-card__salary",
                ".salary"
        );
        String description = ParserUtils.firstText(card,
                ".vacancy-card__description",
                ".job-card__description",
                ".description"
        );
        String dateText = ParserUtils.firstText(card,
                ".vacancy-card__date",
                "time",
                ".date"
        );
        SalaryParser.Salary salary = SalaryParser.parse(salaryText);
        String cleanedDescription = TextUtils.clean(description);

        Vacancy vacancy = new Vacancy();
        vacancy.setExternalId(ParserUtils.externalIdFromUrl(url));
        vacancy.setTitle(title);
        vacancy.setCompany(company);
        vacancy.setCity(city);
        vacancy.setSalaryMin(salary.min());
        vacancy.setSalaryMax(salary.max());
        vacancy.setCurrency(salary.currency());
        vacancy.setDescription(cleanedDescription);
        vacancy.setRequirements(cleanedDescription);
        vacancy.setEmploymentType(EmploymentType.fromText(title + " " + cleanedDescription + " " + city));
        vacancy.setCategory(CategoryClassifier.classify(title, cleanedDescription));
        vacancy.setPublishedDate(ParserUtils.parseDate(dateText));
        vacancy.setUrl(url);
        return vacancy;
    }
}
