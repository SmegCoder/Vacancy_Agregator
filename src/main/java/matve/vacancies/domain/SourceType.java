package matve.vacancies.domain;

public enum SourceType {
    HEADHUNTER,
    HABR_CAREER;

    public static SourceType fromString(String value) {
        for (SourceType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown source type: " + value + ". Allowed: HEADHUNTER, HABR_CAREER");
    }
}
