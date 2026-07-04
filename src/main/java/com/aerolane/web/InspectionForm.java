package com.aerolane.web;

import com.aerolane.model.Inspection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InspectionForm {

    @NotNull(message = "Pick the lane that was inspected")
    private Long laneId;

    @NotNull(message = "Pick the equipment that was inspected")
    private Inspection.EquipmentType equipment;

    @NotNull(message = "Record a pass or fail result")
    private Inspection.Result result;

    @Size(max = 500, message = "Notes are capped at 500 characters")
    private String notes;

    public Long getLaneId() {
        return laneId;
    }

    public void setLaneId(Long laneId) {
        this.laneId = laneId;
    }

    public Inspection.EquipmentType getEquipment() {
        return equipment;
    }

    public void setEquipment(Inspection.EquipmentType equipment) {
        this.equipment = equipment;
    }

    public Inspection.Result getResult() {
        return result;
    }

    public void setResult(Inspection.Result result) {
        this.result = result;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
