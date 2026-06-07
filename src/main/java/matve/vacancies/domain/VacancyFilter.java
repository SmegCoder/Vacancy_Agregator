package matve.vacancies.domain;

public class VacancyFilter {
    private String city;
    private String company;
    private Integer minSalary;
    private EmploymentType employmentType;
    private String keyword;
    private boolean onlyActive = true;

    public String getCity() { return city; }
    public void setCity(String city) { this.city = blankToNull(city); }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = blankToNull(company); }

    public Integer getMinSalary() { return minSalary; }
    public void setMinSalary(Integer minSalary) { this.minSalary = minSalary; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = blankToNull(keyword); }

    public boolean isOnlyActive() { return onlyActive; }
    public void setOnlyActive(boolean onlyActive) { this.onlyActive = onlyActive; }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
