package matve.vacancies.repository;

import matve.vacancies.domain.EmploymentType;
import matve.vacancies.domain.SortField;
import matve.vacancies.domain.Vacancy;
import matve.vacancies.domain.VacancyFilter;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcVacancyRepository implements VacancyRepository {
    private final Database database;

    public JdbcVacancyRepository(Database database) {
        this.database = database;
    }

    @Override
    public UpsertResult upsert(Vacancy vacancy) {
        Optional<Vacancy> existing = findByUrl(vacancy.getUrl());
        LocalDateTime now = LocalDateTime.now();
        if (existing.isEmpty()) {
            vacancy.setCreatedAt(now);
            vacancy.setUpdatedAt(now);
            insert(vacancy);
            return new UpsertResult(vacancy, true, false);
        }
        Vacancy old = existing.get();
        vacancy.setId(old.getId());
        vacancy.setCreatedAt(old.getCreatedAt());
        vacancy.setUpdatedAt(now);
        boolean changed = !Objects.equals(old.getContentHash(), vacancy.getContentHash()) || !old.isActive();
        if (changed) {
            update(vacancy);
        } else if (!old.isActive()) {
            update(vacancy);
        }
        return new UpsertResult(vacancy, false, changed);
    }

    @Override
    public Optional<Vacancy> findById(long id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM vacancies WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot find vacancy", e);
        }
    }

    @Override
    public List<Vacancy> find(VacancyFilter filter, SortField sortField, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM vacancies WHERE 1 = 1");
        List<Param> params = new ArrayList<>();
        if (filter != null && filter.isOnlyActive()) {
            sql.append(" AND active = TRUE");
        }
        if (filter != null && filter.getCity() != null) {
            sql.append(" AND LOWER(city) LIKE ?");
            params.add(new Param("%" + filter.getCity().toLowerCase(Locale.ROOT) + "%"));
        }
        if (filter != null && filter.getCompany() != null) {
            sql.append(" AND LOWER(company) LIKE ?");
            params.add(new Param("%" + filter.getCompany().toLowerCase(Locale.ROOT) + "%"));
        }
        if (filter != null && filter.getMinSalary() != null) {
            sql.append(" AND COALESCE(salary_max, salary_min, 0) >= ?");
            params.add(new Param(filter.getMinSalary()));
        }
        if (filter != null && filter.getEmploymentType() != null && filter.getEmploymentType() != EmploymentType.UNKNOWN) {
            sql.append(" AND employment_type = ?");
            params.add(new Param(filter.getEmploymentType().name()));
        }
        if (filter != null && filter.getKeyword() != null) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(CAST(description AS VARCHAR)) LIKE ? OR LOWER(CAST(requirements AS VARCHAR)) LIKE ?)");
            String value = "%" + filter.getKeyword().toLowerCase(Locale.ROOT) + "%";
            params.add(new Param(value));
            params.add(new Param(value));
            params.add(new Param(value));
        }
        sql.append(orderBy(sortField));
        sql.append(" LIMIT ?");
        params.add(new Param(Math.max(1, limit)));
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                List<Vacancy> vacancies = new ArrayList<>();
                while (rs.next()) {
                    vacancies.add(map(rs));
                }
                return vacancies;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot search vacancies", e);
        }
    }

    @Override
    public List<Vacancy> deactivateMissing(long sourceId, Set<String> actualUrls) {
        List<Vacancy> active = findActiveBySource(sourceId);
        List<Vacancy> removed = new ArrayList<>();
        for (Vacancy vacancy : active) {
            if (!actualUrls.contains(vacancy.getUrl())) {
                vacancy.setActive(false);
                vacancy.setUpdatedAt(LocalDateTime.now());
                update(vacancy);
                removed.add(vacancy);
            }
        }
        return removed;
    }

    @Override
    public long countActive() {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM vacancies WHERE active = TRUE");
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RepositoryException("Cannot count vacancies", e);
        }
    }

    private Optional<Vacancy> findByUrl(String url) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM vacancies WHERE url = ?")) {
            statement.setString(1, url);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot find vacancy by url", e);
        }
    }

    private List<Vacancy> findActiveBySource(long sourceId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM vacancies WHERE source_id = ? AND active = TRUE")) {
            statement.setLong(1, sourceId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Vacancy> vacancies = new ArrayList<>();
                while (rs.next()) {
                    vacancies.add(map(rs));
                }
                return vacancies;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot read active vacancies", e);
        }
    }

    private void insert(Vacancy vacancy) {
        String sql = """
                INSERT INTO vacancies(source_id, source_name, external_id, title, company, city, salary_min, salary_max,
                currency, description, requirements, employment_type, category, published_date, url, active,
                content_hash, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, vacancy);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    vacancy.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot insert vacancy", e);
        }
    }

    private void update(Vacancy vacancy) {
        String sql = """
                UPDATE vacancies SET source_id = ?, source_name = ?, external_id = ?, title = ?, company = ?, city = ?,
                salary_min = ?, salary_max = ?, currency = ?, description = ?, requirements = ?, employment_type = ?,
                category = ?, published_date = ?, url = ?, active = ?, content_hash = ?, created_at = ?, updated_at = ?
                WHERE id = ?
                """;
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, vacancy);
            statement.setLong(20, vacancy.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot update vacancy", e);
        }
    }

    private void fillStatement(PreparedStatement statement, Vacancy vacancy) throws SQLException {
        setLong(statement, 1, vacancy.getSourceId());
        statement.setString(2, vacancy.getSourceName());
        statement.setString(3, vacancy.getExternalId());
        statement.setString(4, vacancy.getTitle());
        statement.setString(5, vacancy.getCompany());
        statement.setString(6, vacancy.getCity());
        setInteger(statement, 7, vacancy.getSalaryMin());
        setInteger(statement, 8, vacancy.getSalaryMax());
        statement.setString(9, vacancy.getCurrency());
        statement.setString(10, vacancy.getDescription());
        statement.setString(11, vacancy.getRequirements());
        statement.setString(12, vacancy.getEmploymentType() == null ? EmploymentType.UNKNOWN.name() : vacancy.getEmploymentType().name());
        statement.setString(13, vacancy.getCategory());
        if (vacancy.getPublishedDate() == null) {
            statement.setNull(14, Types.DATE);
        } else {
            statement.setDate(14, java.sql.Date.valueOf(vacancy.getPublishedDate()));
        }
        statement.setString(15, vacancy.getUrl());
        statement.setBoolean(16, vacancy.isActive());
        statement.setString(17, vacancy.getContentHash());
        statement.setTimestamp(18, Timestamp.valueOf(vacancy.getCreatedAt()));
        statement.setTimestamp(19, Timestamp.valueOf(vacancy.getUpdatedAt()));
    }

    private Vacancy map(ResultSet rs) throws SQLException {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(rs.getLong("id"));
        long sourceId = rs.getLong("source_id");
        vacancy.setSourceId(rs.wasNull() ? null : sourceId);
        vacancy.setSourceName(rs.getString("source_name"));
        vacancy.setExternalId(rs.getString("external_id"));
        vacancy.setTitle(rs.getString("title"));
        vacancy.setCompany(rs.getString("company"));
        vacancy.setCity(rs.getString("city"));
        vacancy.setSalaryMin(getNullableInt(rs, "salary_min"));
        vacancy.setSalaryMax(getNullableInt(rs, "salary_max"));
        vacancy.setCurrency(rs.getString("currency"));
        vacancy.setDescription(rs.getString("description"));
        vacancy.setRequirements(rs.getString("requirements"));
        vacancy.setEmploymentType(EmploymentType.valueOf(rs.getString("employment_type")));
        vacancy.setCategory(rs.getString("category"));
        java.sql.Date date = rs.getDate("published_date");
        vacancy.setPublishedDate(date == null ? null : date.toLocalDate());
        vacancy.setUrl(rs.getString("url"));
        vacancy.setActive(rs.getBoolean("active"));
        vacancy.setContentHash(rs.getString("content_hash"));
        vacancy.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        vacancy.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return vacancy;
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private String orderBy(SortField sortField) {
        SortField field = sortField == null ? SortField.DATE : sortField;
        return switch (field) {
            case SALARY -> " ORDER BY COALESCE(salary_max, salary_min, 0) DESC, id DESC";
            case COMPANY -> " ORDER BY company ASC, id DESC";
            case DATE -> " ORDER BY published_date DESC NULLS LAST, updated_at DESC";
        };
    }

    private void setParams(PreparedStatement statement, List<Param> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i).value();
            if (value instanceof Integer number) {
                statement.setInt(i + 1, number);
            } else {
                statement.setString(i + 1, String.valueOf(value));
            }
        }
    }

    private void setInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, value);
        }
    }

    private record Param(Object value) {
    }
}
