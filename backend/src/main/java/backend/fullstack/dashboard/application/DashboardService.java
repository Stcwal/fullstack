package backend.fullstack.dashboard.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.checklist.domain.ChecklistInstance;
import backend.fullstack.checklist.domain.ChecklistInstanceStatus;
import backend.fullstack.checklist.infrastructure.ChecklistInstanceRepository;
import backend.fullstack.dashboard.api.dto.DashboardAlert;
import backend.fullstack.dashboard.api.dto.DashboardResponse;
import backend.fullstack.dashboard.api.dto.DashboardStats;
import backend.fullstack.dashboard.api.dto.DashboardTask;
import backend.fullstack.deviations.domain.Deviation;
import backend.fullstack.deviations.domain.DeviationSeverity;
import backend.fullstack.deviations.domain.DeviationStatus;
import backend.fullstack.deviations.infrastructure.DeviationRepository;
import backend.fullstack.temperature.infrastructure.TemperatureReadingRepository;
import backend.fullstack.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final TemperatureReadingRepository readingRepository;
    private final DeviationRepository deviationRepository;
    private final ChecklistInstanceRepository checklistInstanceRepository;
    private final UserRepository userRepository;

    public DashboardService(
            TemperatureReadingRepository readingRepository,
            DeviationRepository deviationRepository,
            ChecklistInstanceRepository checklistInstanceRepository,
            UserRepository userRepository
    ) {
        this.readingRepository = readingRepository;
        this.deviationRepository = deviationRepository;
        this.checklistInstanceRepository = checklistInstanceRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse getDashboard(Long organizationId, Long locationId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        long tempAlerts = readingRepository
                .countDeviationsAfterByOrganizationAndOptionalLocation(organizationId, startOfToday, locationId);
        long openDeviations = deviationRepository
                .countByOrganizationAndStatusAndOptionalLocation(organizationId, DeviationStatus.OPEN, locationId);

        List<DashboardTask> tasks = buildTasks(organizationId, locationId, startOfToday);
        List<DashboardAlert> alerts = buildAlerts(organizationId, locationId);

        int total = tasks.size();
        int completed = (int) tasks.stream()
                .filter(t -> "COMPLETED".equals(t.status()))
                .count();
        int compliancePercent = total == 0 ? 100 : (int) Math.round((double) completed / total * 100);

        DashboardStats stats = new DashboardStats(completed, total, tempAlerts, openDeviations, compliancePercent);
        return new DashboardResponse(stats, tasks, alerts);
    }

    private List<DashboardTask> buildTasks(Long organizationId, Long locationId, LocalDateTime startOfToday) {
        LocalDate today = startOfToday.toLocalDate();

        return checklistInstanceRepository
                .findAllByOrganizationIdAndOptionalLocation(organizationId, locationId).stream()
                .filter(instance -> today.equals(instance.getDate()))
                .map(this::toTask)
                .toList();
    }

    private DashboardTask toTask(ChecklistInstance instance) {
        boolean isCompleted = ChecklistInstanceStatus.COMPLETED.equals(instance.getStatus());
        String status = isCompleted ? "COMPLETED" : "NOT_STARTED";

        // ChecklistInstance has no completedBy/completedAt fields — derive from items
        String completedBy = null;
        String completedAt = null;
        if (isCompleted) {
            // Best-effort: find the latest completed item's timestamp for completedAt
            completedAt = instance.getItems().stream()
                    .filter(item -> item.getCompletedAt() != null)
                    .map(item -> formatInstant(item.getCompletedAt()))
                    .reduce((first, second) -> second)
                    .orElse(null);
            completedBy = instance.getItems().stream()
                    .filter(item -> item.getCompletedByUserId() != null)
                    .map(item -> item.getCompletedByUserId())
                    .findFirst()
                    .flatMap(userRepository::findById)
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse(null);
        }

        return new DashboardTask(instance.getId(), instance.getTitle(), status, completedBy, completedAt);
    }

    private List<DashboardAlert> buildAlerts(Long organizationId, Long locationId) {
        return deviationRepository
                .findByOrganizationAndStatusAndOptionalLocation(organizationId, DeviationStatus.OPEN, locationId)
                .stream()
                .limit(5)
                .map(this::toAlert)
                .toList();
    }

    private static String formatInstant(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalTime().format(TIME_FORMAT);
    }

    private DashboardAlert toAlert(Deviation deviation) {
        long minutesSince = ChronoUnit.MINUTES.between(deviation.getCreatedAt(), LocalDateTime.now());
        String timeLabel;
        if (minutesSince < 60) {
            timeLabel = minutesSince + " min siden";
        } else {
            long hours = minutesSince / 60;
            long mins  = minutesSince % 60;
            timeLabel  = hours + "t " + (mins > 0 ? mins + "min " : "") + "siden";
        }
        String message = deviation.getTitle() + " — " + timeLabel;

        DeviationSeverity severity = deviation.getSeverity();
        String type = (severity == DeviationSeverity.CRITICAL || severity == DeviationSeverity.HIGH)
                ? "danger"
                : "warning";

        String time = deviation.getCreatedAt().format(TIME_FORMAT);

        return new DashboardAlert(deviation.getId(), message, type, time);
    }
}
