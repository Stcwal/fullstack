package backend.fullstack.reports.api.dto;

import java.util.List;

public record ChartResponse(List<String> labels, List<ChartDataset> datasets, List<ChartAlert> alerts) {}
