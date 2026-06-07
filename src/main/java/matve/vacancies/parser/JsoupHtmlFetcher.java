package matve.vacancies.parser;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsoupHtmlFetcher implements HtmlFetcher {
    private static final int TIMEOUT_MILLIS = 15_000;

    @Override
    public String fetch(String url) {
        try {
            if (url.startsWith("file:")) {
                return Files.readString(Path.of(URI.create(url)));
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return Files.readString(Path.of(url));
            }
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 VacancyAggregator/1.0")
                    .timeout(TIMEOUT_MILLIS)
                    .get()
                    .html();
        } catch (IOException e) {
            throw new ParserException("Cannot fetch page: " + url, e);
        }
    }
}
