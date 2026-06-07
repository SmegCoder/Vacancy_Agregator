package matve.vacancies.repository;

import matve.vacancies.domain.AlertRule;
import matve.vacancies.domain.EmploymentType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAlertRuleRepository implements AlertRuleRepository {
    private final Database database;

    public JdbcAlertRuleRepository(Database database) {
        this.database = database;
    }

    @Override
    public AlertRule save(AlertRule rule) {
        String sql = "INSERT INTO alert_rules(name, city, company, min_salary, keyword, employment_type, enabled) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, rule.getName());
            statement.setString(2, rule.getCity());
            statement.setString(3, rule.getCompany());
            if (rule.getMinSalary() == null) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, rule.getMinSalary());
            }
            statement.setString(5, rule.getKeyword());
            statement.setString(6, rule.getEmploymentType() == null ? EmploymentType.UNKNOWN.name() : rule.getEmploymentType().name());
            statement.setBoolean(7, rule.isEnabled());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    rule.setId(keys.getLong(1));
                }
            }
            return rule;
        } catch (SQLException e) {
            throw new RepositoryException("Cannot save alert rule", e);
        }
    }

    @Override
    public List<AlertRule> findAll() {
        return find("SELECT * FROM alert_rules ORDER BY id");
    }

    @Override
    public List<AlertRule> findEnabled() {
        return find("SELECT * FROM alert_rules WHERE enabled = TRUE ORDER BY id");
    }

    private List<AlertRule> find(String sql) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<AlertRule> rules = new ArrayList<>();
            while (rs.next()) {
                AlertRule rule = new AlertRule();
                rule.setId(rs.getLong("id"));
                rule.setName(rs.getString("name"));
                rule.setCity(rs.getString("city"));
                rule.setCompany(rs.getString("company"));
                int minSalary = rs.getInt("min_salary");
                rule.setMinSalary(rs.wasNull() ? null : minSalary);
                rule.setKeyword(rs.getString("keyword"));
                rule.setEmploymentType(EmploymentType.valueOf(rs.getString("employment_type")));
                rule.setEnabled(rs.getBoolean("enabled"));
                rules.add(rule);
            }
            return rules;
        } catch (SQLException e) {
            throw new RepositoryException("Cannot read alert rules", e);
        }
    }
}
