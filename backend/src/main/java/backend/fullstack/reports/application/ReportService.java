package backend.fullstack.reports.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.temperature.infrastructure.TemperatureReadingRepository;
import backend.fullstack.reports.api.dto.ChartAlert;
import backend.fullstack.reports.api.dto.ChartDataset;
import backend.fullstack.reports.api.dto.ChartResponse;
import backend.fullstack.units.domain.TemperatureUnit;
import backend.fullstack.units.infrastructure.TemperatureUnitRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final String[] PALETTE = {
        "#3B82F6", "#6366F1", "#16A34A", "#F59E0B", "#EF4444", "#8B5CF6"
    };

    private final TemperatureUnitRepository unitRepository;
    private final TemperatureReadingRepository readingRepository;

    public ReportService(
            TemperatureUnitRepository unitRepository,
            TemperatureReadingRepository readingRepository
    ) {
        this.unitRepository = unitRepository;
        this.readingRepository = readingRepository;
    }

    public ChartResponse getChartData(Long organizationId, String period) {
        int days = "MONTH".equalsIgnoreCase(period) ? 30 : 7;
        LocalDateTime since = LocalDate.now().minusDays(days - 1L).atStartOfDay();

        List<String> labels = buildLabels(days);
        List<TemperatureUnit> units = unitRepository.findByOrganizationAndOptionalActive(organizationId, true, null);

        List<ChartDataset> datasets = new ArrayList<>();
        List<ChartAlert> alerts = new ArrayList<>();

        for (int i = 0; i < units.size(); i++) {
            TemperatureUnit unit = units.get(i);
            List<TemperatureReading> readings = readingRepository
                    .findByOrganization_IdAndUnit_IdOrderByRecordedAtDesc(organizationId, unit.getId());

            List<TemperatureReading> periodReadings = readings.stream()
                    .filter(r -> !r.getRecordedAt().isBefore(since))
                    .toList();

            List<Double> data = buildDailyAverages(periodReadings, days);

            String color = PALETTE[i % PALETTE.length];
            datasets.add(new ChartDataset(unit.getName(), data, color, unit.getMinThreshold(), unit.getMaxThreshold(), unit.getType().name()));

            for (int dayIdx = 0; dayIdx < days; dayIdx++) {
                addAlertIfOutOfRange(periodReadings, dayIdx, days, unit.getName(), alerts);
            }
        }

        return new ChartResponse(labels, datasets, alerts);
    }

    private List<String> buildLabels(int days) {
        List<String> labels = new ArrayList<>();
        LocalDate today = LocalDate.now();
        if (days == 7) {
            // Mon=1..Sun=7, Norwegian abbreviations; Sunday maps to index 0
            String[] daysNo = {"Søn", "Man", "Tir", "Ons", "Tor", "Fre", "Lør"};
            for (int i = days - 1; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                String dayName = daysNo[d.getDayOfWeek().getValue() % 7];
                labels.add(dayName + " " + d.getDayOfMonth());
            }
        } else {
            for (int i = days - 1; i >= 0; i--) {
                labels.add(String.valueOf(today.minusDays(i).getDayOfMonth()));
            }
        }
        return labels;
    }

    private List<Double> buildDailyAverages(List<TemperatureReading> readings, int days) {
        LocalDate today = LocalDate.now();
        List<Double> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            OptionalDouble avg = readings.stream()
                    .filter(r -> r.getRecordedAt().toLocalDate().equals(day))
                    .mapToDouble(TemperatureReading::getTemperature)
                    .average();
            result.add(avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : null);
        }
        return result;
    }

    private void addAlertIfOutOfRange(
            List<TemperatureReading> periodReadings,
            int dayIdx,
            int days,
            String unitName,
            List<ChartAlert> alerts
    ) {
        LocalDate today = LocalDate.now();
        // dayIdx 0 = oldest day, dayIdx (days-1) = today
        LocalDate day = today.minusDays(days - 1L - dayIdx);

        periodReadings.stream()
                .filter(r -> r.isDeviation() && r.getRecordedAt().toLocalDate().equals(day))
                .findFirst()
                .ifPresent(r -> alerts.add(new ChartAlert(dayIdx, unitName, r.getTemperature(), "OPEN")));
    }
}
