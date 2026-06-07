package matve.vacancies.domain;

import java.time.LocalDateTime;

public class ChangeEvent {
    private Long id;
    private Long vacancyId;
    private String type;
    private String message;
    private LocalDateTime createdAt;

    public ChangeEvent() {
    }

    public ChangeEvent(Long vacancyId, String type, String message) {
        this.vacancyId = vacancyId;
        this.type = type;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVacancyId() { return vacancyId; }
    public void setVacancyId(Long vacancyId) { this.vacancyId = vacancyId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
