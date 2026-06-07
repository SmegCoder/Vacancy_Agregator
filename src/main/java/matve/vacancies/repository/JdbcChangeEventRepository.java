package matve.vacancies.repository;

import matve.vacancies.domain.ChangeEvent;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcChangeEventRepository implements ChangeEventRepository {
    private final Database database;

    public JdbcChangeEventRepository(Database database) {
        this.database = database;
    }

    @Override
    public ChangeEvent save(ChangeEvent event) {
        String sql = "INSERT INTO change_events(vacancy_id, type, message, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (event.getVacancyId() == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, event.getVacancyId());
            }
            statement.setString(2, event.getType());
            statement.setString(3, event.getMessage());
            statement.setTimestamp(4, Timestamp.valueOf(event.getCreatedAt() == null ? LocalDateTime.now() : event.getCreatedAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    event.setId(keys.getLong(1));
                }
            }
            return event;
        } catch (SQLException e) {
            throw new RepositoryException("Cannot save change event", e);
        }
    }

    @Override
    public List<ChangeEvent> latest(int limit) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM change_events ORDER BY created_at DESC, id DESC LIMIT ?")) {
            statement.setInt(1, Math.max(1, limit));
            try (ResultSet rs = statement.executeQuery()) {
                List<ChangeEvent> events = new ArrayList<>();
                while (rs.next()) {
                    ChangeEvent event = new ChangeEvent();
                    event.setId(rs.getLong("id"));
                    long vacancyId = rs.getLong("vacancy_id");
                    event.setVacancyId(rs.wasNull() ? null : vacancyId);
                    event.setType(rs.getString("type"));
                    event.setMessage(rs.getString("message"));
                    event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    events.add(event);
                }
                return events;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Cannot read change events", e);
        }
    }
}
