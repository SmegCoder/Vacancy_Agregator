package matve.vacancies.repository;

import matve.vacancies.domain.Source;
import matve.vacancies.domain.SourceType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSourceRepository implements SourceRepository {
    private final Database database;

    public JdbcSourceRepository(Database database) {
        this.database = database;
    }

    @Override
    public Source save(Source source) {
        String sql = "INSERT INTO sources(name, type, url_template, enabled, max_pages) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, source.getName());
            statement.setString(2, source.getType().name());
            statement.setString(3, source.getUrlTemplate());
            statement.setBoolean(4, source.isEnabled());
            statement.setInt(5, source.getMaxPages());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    source.setId(keys.getLong(1));
                }
            }
            return source;
        } catch (SQLException e) {
            throw new RepositoryException("Cannot save source", e);
        }
    }

    @Override
    public List<Source> findAll() {
        return findBySql("SELECT * FROM sources ORDER BY id");
    }

    @Override
    public List<Source> findEnabled() {
        return findBySql("SELECT * FROM sources WHERE enabled = TRUE ORDER BY id");
    }

    @Override
    public Optional<Source> findById(long id) {
        String sql = "SELECT * FROM sources WHERE id = ?";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot find source", e);
        }
    }

    @Override
    public void setEnabled(long id, boolean enabled) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE sources SET enabled = ? WHERE id = ?")) {
            statement.setBoolean(1, enabled);
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot update source", e);
        }
    }

    @Override
    public void delete(long id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM sources WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Cannot delete source", e);
        }
    }

    private List<Source> findBySql(String sql) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Source> sources = new ArrayList<>();
            while (resultSet.next()) {
                sources.add(map(resultSet));
            }
            return sources;
        } catch (SQLException e) {
            throw new RepositoryException("Cannot read sources", e);
        }
    }

    private Source map(ResultSet resultSet) throws SQLException {
        return new Source(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                SourceType.fromString(resultSet.getString("type")),
                resultSet.getString("url_template"),
                resultSet.getBoolean("enabled"),
                resultSet.getInt("max_pages")
        );
    }
}
