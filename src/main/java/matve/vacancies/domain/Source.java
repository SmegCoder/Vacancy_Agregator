package matve.vacancies.domain;

public class Source {
    private Long id;
    private String name;
    private SourceType type;
    private String urlTemplate;
    private boolean enabled;
    private int maxPages;

    public Source() {
        this.enabled = true;
        this.maxPages = 1;
    }

    public Source(Long id, String name, SourceType type, String urlTemplate, boolean enabled, int maxPages) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.urlTemplate = urlTemplate;
        this.enabled = enabled;
        this.maxPages = maxPages;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SourceType getType() { return type; }
    public void setType(SourceType type) { this.type = type; }

    public String getUrlTemplate() { return urlTemplate; }
    public void setUrlTemplate(String urlTemplate) { this.urlTemplate = urlTemplate; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getMaxPages() { return maxPages; }
    public void setMaxPages(int maxPages) { this.maxPages = Math.max(1, maxPages); }

    public String pageUrl(int page) {
        if (urlTemplate.contains("{page}")) {
            return urlTemplate.replace("{page}", String.valueOf(page));
        }
        return urlTemplate;
    }
}
