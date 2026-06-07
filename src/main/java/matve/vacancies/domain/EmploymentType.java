package matve.vacancies.domain;

public enum EmploymentType {
    FULL_TIME,
    PART_TIME,
    REMOTE,
    INTERNSHIP,
    PROJECT,
    UNKNOWN;

    public static EmploymentType fromText(String text) {
        if (text == null || text.isBlank()) {
            return UNKNOWN;
        }
        String normalized = text.toLowerCase();
        if (normalized.contains("удален") || normalized.contains("remote") || normalized.contains("дистанц")) {
            return REMOTE;
        }
        if (normalized.contains("стаж") || normalized.contains("intern")) {
            return INTERNSHIP;
        }
        if (normalized.contains("part") || normalized.contains("частич") || normalized.contains("непол")) {
            return PART_TIME;
        }
        if (normalized.contains("проект") || normalized.contains("contract") || normalized.contains("temporary")) {
            return PROJECT;
        }
        if (normalized.contains("full") || normalized.contains("полный") || normalized.contains("офис") || normalized.contains("5/2")) {
            return FULL_TIME;
        }
        return UNKNOWN;
    }
}
