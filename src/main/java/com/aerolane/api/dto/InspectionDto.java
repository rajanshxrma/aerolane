package com.aerolane.api.dto;

import com.aerolane.model.Inspection;
import java.time.LocalDateTime;

public record InspectionDto(
        Long id,
        Long laneId,
        String laneName,
        Inspection.EquipmentType equipment,
        Inspection.Result result,
        String notes,
        String inspectedBy,
        LocalDateTime createdAt) {

    public static InspectionDto from(Inspection inspection) {
        return new InspectionDto(
                inspection.getId(),
                inspection.getLane().getId(),
                inspection.getLane().getName(),
                inspection.getEquipment(),
                inspection.getResult(),
                inspection.getNotes(),
                inspection.getInspectedBy(),
                inspection.getCreatedAt());
    }
}
