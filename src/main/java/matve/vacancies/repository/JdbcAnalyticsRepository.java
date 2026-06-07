package matve.vacancies.repository;

import matve.vacancies.domain.CountStat;
import matve.vacancies.domain.SalaryStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAnalyticsRepository implements AnalyticsRepository {
    private final Database database;

    public JdbcAnalyticsRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<CountStat> countByCategory() {
        return count("category");
    }

    @Override
    public List<CountStat> countByCity() {
        return count("city");
    }

    @Override
    public List<SalaryStats> salaryByCategory() {
        return salary("category");
    }

    @Override
    public List<SalaryStats> salaryByCity() {
        return salary("city");
    }

    private List<CountStat> count(String column) {
        String sql = "SELECT COALESCE(" + column + ", 'Не указано') AS group_name, COUNT(*) AS cnt " +
                "FROM vacancies WHERE active = TRUE GROUP BY COALESCE(" + column + ", 'Не указано') ORDER BY cnt DESC";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<CountStat> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new CountStat(rs.getString("group_name"), rs.getInt("cnt")));
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Cannot calculate count statistics", e);
        }
    }

    private List<SalaryStats> salary(String column) {
        String sql = "SELECT COALESCE(" + column + ", 'Не указано') AS group_name, COUNT(*) AS cnt, " +
                "MIN(COALESCE(salary_max, salary_min)) AS min_salary, " +
                "AVG(COALESCE(salary_max, salary_min)) AS avg_salary, " +
                "MAX(COALESCE(salary_max, salary_min)) AS max_salary " +
                "FROM vacancies WHERE active = TRUE AND COALESCE(salary_max, salary_min) IS NOT NULL " +
                "GROUP BY COALESCE(" + column + ", 'Не указано') ORDER BY avg_salary DESC";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<SalaryStats> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new SalaryStats(
                        rs.getString("group_name"),
                        rs.getInt("cnt"),
                        rs.getInt("min_salary"),
                        (int) Math.round(rs.getDouble("avg_salary")),
                        rs.getInt("max_salary")
                ));
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Cannot calculate salary statistics", e);
        }
    }
}
