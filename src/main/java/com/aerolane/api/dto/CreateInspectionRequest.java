package com.aerolane.api.dto;

import com.aerolane.model.Inspection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInspectionRequest(
        @NotNull(message = "laneId is required") Long laneId,
        @NotNull(message = "equipment is required") Inspection.EquipmentType equipment,
        @NotNull(message = "result is required") Inspection.Result result,
        @Size(max = 500, message = "notes are capped at 500 characters") String notes) {
}
