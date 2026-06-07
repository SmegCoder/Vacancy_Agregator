package matve.vacancies.domain;

public class CountStat {
    private final String name;
    private final int count;

    public CountStat(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() { return name; }
    public int getCount() { return count; }
}
