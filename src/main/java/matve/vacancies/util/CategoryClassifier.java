package matve.vacancies.util;

import java.util.Locale;

public final class CategoryClassifier {
    private CategoryClassifier() {
    }

    public static String classify(String title, String description) {
        String text = (TextUtils.safe(title) + " " + TextUtils.safe(description)).toLowerCase(Locale.ROOT);
        if (containsAny(text, "java", "spring", "backend", "бэкенд", "scala", "kotlin")) {
            return "Backend";
        }
        if (containsAny(text, "frontend", "react", "vue", "angular", "javascript", "typescript")) {
            return "Frontend";
        }
        if (containsAny(text, "android", "ios", "mobile", "swift")) {
            return "Mobile";
        }
        if (containsAny(text, "data", "ml", "machine learning", "аналитик", "analyst", "python")) {
            return "Data/ML";
        }
        if (containsAny(text, "qa", "тестиров", "tester", "quality")) {
            return "QA";
        }
        if (containsAny(text, "devops", "sre", "kubernetes", "docker")) {
            return "DevOps";
        }
        return "Other";
    }

    private static boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
