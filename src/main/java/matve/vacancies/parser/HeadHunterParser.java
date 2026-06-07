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

public class HeadHunterParser implements VacancyParser {
    @Override
    public List<Vacancy> parse(Source source, String html, String pageUrl) {
        Document document = Jsoup.parse(html, pageUrl);
        Elements cards = document.select("[data-qa=vacancy-serp__vacancy], .vacancy-serp-item, .serp-item");
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
                "a[data-qa=serp-item__title]",
                "span[data-qa=serp-item__title-text]",
                ".serp-item__title",
                "a[href*='/vacancy/']"
        );
        String href = ParserUtils.firstAttr(card, "href",
                "a[data-qa=serp-item__title]",
                "a[href*='/vacancy/']"
        );
        String url = ParserUtils.normalizeUrl(pageUrl, href);
        if (title.isBlank() || url.isBlank()) {
            return null;
        }

        String company = ParserUtils.firstText(card,
                "[data-qa=vacancy-serp__vacancy-employer-text]",
                "[data-qa=vacancy-serp__vacancy-employer]",
                ".vacancy-serp-item__meta-info-company",
                ".company"
        );
        String city = ParserUtils.firstText(card,
                "[data-qa=vacancy-serp__vacancy-address]",
                "[data-qa=vacancy-serp__vacancy-address-narrow]",
                ".vacancy-serp-item__info",
                ".city"
        );
        String salaryText = ParserUtils.firstText(card,
                "[data-qa=vacancy-serp__vacancy-compensation]",
                "[data-qa=vacancy-serp__vacancy-salary]",
                ".vacancy-serp-item__sidebar",
                ".salary"
        );
        String requirement = ParserUtils.firstText(card,
                "[data-qa=vacancy-serp__vacancy_snippet_requirement]",
                ".vacancy-serp-item__snippet",
                ".requirements"
        );
        String responsibility = ParserUtils.firstText(card,
                "[data-qa=vacancy-serp__vacancy_snippet_responsibility]",
                ".description"
        );
        String dateText = ParserUtils.firstText(card,
                "[data-qa=vacancy-serp__vacancy-date]",
                "time",
                ".date"
        );
        SalaryParser.Salary salary = SalaryParser.parse(salaryText);
        String description = TextUtils.clean(responsibility + " " + requirement);

        Vacancy vacancy = new Vacancy();
        vacancy.setExternalId(ParserUtils.externalIdFromUrl(url));
        vacancy.setTitle(title);
        vacancy.setCompany(company);
        vacancy.setCity(city);
        vacancy.setSalaryMin(salary.min());
        vacancy.setSalaryMax(salary.max());
        vacancy.setCurrency(salary.currency());
        vacancy.setDescription(description);
        vacancy.setRequirements(requirement);
        vacancy.setEmploymentType(EmploymentType.fromText(title + " " + description + " " + city));
        vacancy.setCategory(CategoryClassifier.classify(title, description));
        vacancy.setPublishedDate(ParserUtils.parseDate(dateText));
        vacancy.setUrl(url);
        return vacancy;
    }
}
