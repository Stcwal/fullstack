package backend.fullstack.dashboard.api.dto;

public record DashboardAlert(
        Long id,
        String message,
        String type,
        String time
) {}
