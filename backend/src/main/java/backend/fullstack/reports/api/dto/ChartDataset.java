package backend.fullstack.reports.api.dto;

import java.util.List;

public record ChartDataset(String label, List<Double> data, String color) {}
