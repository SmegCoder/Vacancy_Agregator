package matve.vacancies.domain;

public enum SortField {
    DATE,
    SALARY,
    COMPANY;

    public static SortField fromString(String value) {
        if (value == null || value.isBlank()) {
            return DATE;
        }
        for (SortField field : values()) {
            if (field.name().equalsIgnoreCase(value)) {
                return field;
            }
        }
        return DATE;
    }
}
