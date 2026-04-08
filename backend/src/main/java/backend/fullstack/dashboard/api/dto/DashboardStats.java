package backend.fullstack.dashboard.api.dto;

public record DashboardStats(
        int tasksCompleted,
        int tasksTotal,
        long tempAlerts,
        long openDeviations,
        int compliancePercent
) {}
