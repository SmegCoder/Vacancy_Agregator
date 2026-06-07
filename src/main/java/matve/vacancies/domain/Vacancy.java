package matve.vacancies.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Vacancy {
    private Long id;
    private Long sourceId;
    private String sourceName;
    private String externalId;
    private String title;
    private String company;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private String description;
    private String requirements;
    private EmploymentType employmentType;
    private String category;
    private LocalDate publishedDate;
    private String url;
    private boolean active;
    private String contentHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Vacancy() {
        this.employmentType = EmploymentType.UNKNOWN;
        this.currency = "RUB";
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }

    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType == null ? EmploymentType.UNKNOWN : employmentType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int averageSalary() {
        if (salaryMin != null && salaryMax != null) {
            return (salaryMin + salaryMax) / 2;
        }
        if (salaryMin != null) {
            return salaryMin;
        }
        if (salaryMax != null) {
            return salaryMax;
        }
        return 0;
    }

    public String shortSalary() {
        if (salaryMin == null && salaryMax == null) {
            return "не указана";
        }
        String currentCurrency = currency == null || currency.isBlank() ? "RUB" : currency;
        if (salaryMin != null && salaryMax != null) {
            return salaryMin + "-" + salaryMax + " " + currentCurrency;
        }
        if (salaryMin != null) {
            return "от " + salaryMin + " " + currentCurrency;
        }
        return "до " + salaryMax + " " + currentCurrency;
    }

    public String searchableText() {
        return String.join(" ", safe(title), safe(company), safe(city), safe(description), safe(requirements), safe(category));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vacancy vacancy)) return false;
        return Objects.equals(url, vacancy.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
