package matve.vacancies.domain;

public class AlertRule {
    private Long id;
    private String name;
    private String city;
    private String company;
    private Integer minSalary;
    private String keyword;
    private EmploymentType employmentType;
    private boolean enabled;

    public AlertRule() {
        this.enabled = true;
        this.employmentType = EmploymentType.UNKNOWN;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public Integer getMinSalary() { return minSalary; }
    public void setMinSalary(Integer minSalary) { this.minSalary = minSalary; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType == null ? EmploymentType.UNKNOWN : employmentType; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
