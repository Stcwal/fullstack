package backend.fullstack.readings.api.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import backend.fullstack.readings.domain.TemperatureReading;

@Mapper(componentModel = "spring")
public interface ReadingMapper {

    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "recordedBy", source = "recordedByName")
    @Mapping(target = "isOutOfRange", source = "outOfRange")
    ReadingResponse toResponse(TemperatureReading reading);
}
