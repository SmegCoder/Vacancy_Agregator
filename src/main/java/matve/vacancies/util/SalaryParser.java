package matve.vacancies.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SalaryParser {
    private static final Pattern NUMBER = Pattern.compile("(\\d[\\d\\s]*)");

    private SalaryParser() {
    }

    public static Salary parse(String text) {
        String cleaned = TextUtils.clean(text);
        if (cleaned.isBlank()) {
            return new Salary(null, null, "RUB");
        }
        String lower = cleaned.toLowerCase(Locale.ROOT);
        String currency = detectCurrency(lower);
        Matcher matcher = NUMBER.matcher(lower.replace("\u00A0", " "));
        Integer first = null;
        Integer second = null;
        if (matcher.find()) {
            first = parseInt(matcher.group(1));
        }
        if (matcher.find()) {
            second = parseInt(matcher.group(1));
        }
        if (first == null) {
            return new Salary(null, null, currency);
        }
        if (lower.contains("до ") || lower.startsWith("до")) {
            return new Salary(null, first, currency);
        }
        if (second != null) {
            return new Salary(first, second, currency);
        }
        return new Salary(first, null, currency);
    }

    private static String detectCurrency(String text) {
        if (text.contains("$") || text.contains("usd")) {
            return "USD";
        }
        if (text.contains("€") || text.contains("eur")) {
            return "EUR";
        }
        if (text.contains("₽") || text.contains("руб") || text.contains("rur")) {
            return "RUB";
        }
        return "RUB";
    }

    private static Integer parseInt(String raw) {
        String digits = raw.replaceAll("\\D+", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public record Salary(Integer min, Integer max, String currency) {
    }
}
