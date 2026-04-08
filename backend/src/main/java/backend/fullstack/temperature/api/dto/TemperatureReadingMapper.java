package backend.fullstack.temperature.api.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import backend.fullstack.temperature.domain.TemperatureReading;
import backend.fullstack.user.User;

@Mapper(componentModel = "spring")
public interface TemperatureReadingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "recordedBy", ignore = true)
    @Mapping(target = "isDeviation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "recordedAt", expression = "java(request.recordedAt() != null ? request.recordedAt() : java.time.LocalDateTime.now())")
    TemperatureReading toEntity(TemperatureReadingRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitName", source = "unit.name")
    @Mapping(target = "targetTemperature", source = "unit.targetTemperature")
    @Mapping(target = "minThreshold", source = "unit.minThreshold")
    @Mapping(target = "maxThreshold", source = "unit.maxThreshold")
    @Mapping(target = "recordedBy", source = "recordedBy", qualifiedByName = "toRecordedByResponse")
    @Mapping(target = "isDeviation", source = "deviation")
    TemperatureReadingResponse toResponse(TemperatureReading reading);

    @Named("toRecordedByResponse")
    default RecordedByResponse toRecordedByResponse(User user) {
        if (user == null) {
            return null;
        }

        return new RecordedByResponse(user.getId(), user.getFirstName() + " " + user.getLastName());
    }
}