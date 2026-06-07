package matve.vacancies.service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoRefreshService {
    private final AggregationService aggregationService;
    private final AlertService alertService;
    private ScheduledExecutorService executorService;

    public AutoRefreshService(AggregationService aggregationService, AlertService alertService) {
        this.aggregationService = aggregationService;
        this.alertService = alertService;
    }

    public synchronized boolean start(long seconds, Output output) {
        if (isRunning()) {
            return false;
        }
        long interval = Math.max(10, seconds);
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                AggregationResult result = aggregationService.refreshAll();
                output.println("\n[auto-refresh] Загружено: " + result.getFetched()
                        + ", новых: " + result.getAdded().size()
                        + ", обновлено: " + result.getUpdated().size()
                        + ", удалено: " + result.getRemoved().size());
                List<String> notifications = alertService.matchNewVacancies(result.getAdded());
                for (String notification : notifications) {
                    output.println("[alert] " + notification);
                }
                output.printPrompt();
            } catch (RuntimeException e) {
                output.println("\n[auto-refresh] Ошибка: " + e.getMessage());
                output.printPrompt();
            }
        }, interval, interval, TimeUnit.SECONDS);
        return true;
    }

    public synchronized void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    public synchronized boolean isRunning() {
        return executorService != null && !executorService.isShutdown();
    }

    public interface Output {
        void println(String text);
        void printPrompt();
    }
}
