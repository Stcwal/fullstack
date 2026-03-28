package backend.fullstack.location.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignLocationsRequest {

    @NotNull
    @Size(min = 1, message = "At least one location must be assigned")
    private List<Long> locationIds;
}
