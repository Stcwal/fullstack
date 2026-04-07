package backend.fullstack.dashboard.api.dto;

import java.util.List;

public record DashboardResponse(
        DashboardStats stats,
        List<DashboardTask> tasks,
        List<DashboardAlert> alerts
) {}
