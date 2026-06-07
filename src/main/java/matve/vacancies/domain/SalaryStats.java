package matve.vacancies.domain;

public class SalaryStats {
    private final String groupName;
    private final int count;
    private final int minSalary;
    private final int averageSalary;
    private final int maxSalary;

    public SalaryStats(String groupName, int count, int minSalary, int averageSalary, int maxSalary) {
        this.groupName = groupName;
        this.count = count;
        this.minSalary = minSalary;
        this.averageSalary = averageSalary;
        this.maxSalary = maxSalary;
    }

    public String getGroupName() { return groupName; }
    public int getCount() { return count; }
    public int getMinSalary() { return minSalary; }
    public int getAverageSalary() { return averageSalary; }
    public int getMaxSalary() { return maxSalary; }
}
