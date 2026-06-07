package matve.vacancies.parser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import matve.vacancies.util.TextUtils;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ParserUtils {
    private static final Pattern ISO_DATE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern RU_DATE = Pattern.compile("(\\d{1,2})\\s+(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)\\s*(\\d{4})?");

    private ParserUtils() {
    }

    public static String firstText(Element element, String... selectors) {
        for (String selector : selectors) {
            Elements found = element.select(selector);
            if (!found.isEmpty()) {
                String text = TextUtils.clean(found.first().text());
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }

    public static String firstAttr(Element element, String attribute, String... selectors) {
        for (String selector : selectors) {
            Elements found = element.select(selector);
            if (!found.isEmpty()) {
                String value = TextUtils.clean(found.first().attr(attribute));
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return "";
    }

    public static String normalizeUrl(String baseUrl, String href) {
        if (href == null || href.isBlank()) {
            return "";
        }
        try {
            URI base = URI.create(baseUrl);
            URI resolved = base.resolve(href);
            URI withoutFragment = new URI(resolved.getScheme(), resolved.getAuthority(), resolved.getPath(), resolved.getQuery(), null);
            return withoutFragment.toString();
        } catch (Exception e) {
            return href;
        }
    }

    public static String externalIdFromUrl(String url) {
        Matcher matcher = Pattern.compile("/(?:vacancy|jobs|job)/(\\d+)").matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return TextUtils.hash(url).substring(0, 16);
    }

    public static LocalDate parseDate(String text) {
        String cleaned = TextUtils.clean(text).toLowerCase(Locale.ROOT);
        if (cleaned.isBlank()) {
            return LocalDate.now();
        }
        if (cleaned.contains("сегодня")) {
            return LocalDate.now();
        }
        if (cleaned.contains("вчера")) {
            return LocalDate.now().minusDays(1);
        }
        Matcher iso = ISO_DATE.matcher(cleaned);
        if (iso.find()) {
            try {
                return LocalDate.parse(iso.group(1), DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException ignored) {
                return LocalDate.now();
            }
        }
        Matcher ru = RU_DATE.matcher(cleaned);
        if (ru.find()) {
            int day = Integer.parseInt(ru.group(1));
            int month = monthNumber(ru.group(2));
            int year = ru.group(3) == null ? LocalDate.now().getYear() : Integer.parseInt(ru.group(3));
            return LocalDate.of(year, month, day);
        }
        return LocalDate.now();
    }

    private static int monthNumber(String month) {
        return switch (month) {
            case "января" -> 1;
            case "февраля" -> 2;
            case "марта" -> 3;
            case "апреля" -> 4;
            case "мая" -> 5;
            case "июня" -> 6;
            case "июля" -> 7;
            case "августа" -> 8;
            case "сентября" -> 9;
            case "октября" -> 10;
            case "ноября" -> 11;
            case "декабря" -> 12;
            default -> LocalDate.now().getMonthValue();
        };
    }
}
