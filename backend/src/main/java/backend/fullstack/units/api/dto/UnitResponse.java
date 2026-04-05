package backend.fullstack.units.api.dto;

import java.time.LocalDateTime;

import backend.fullstack.units.domain.UnitType;

public record UnitResponse(
        Long id,
        Long organizationId,
        String name,
        UnitType type,
        Double targetTemperature,
        Double minThreshold,
        Double maxThreshold,
        String description,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
