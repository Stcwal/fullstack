package backend.fullstack.dashboard.api.dto;

public record DashboardTask(
        Long id,
        String name,
        String status,
        String completedBy,
        String completedAt
) {}
