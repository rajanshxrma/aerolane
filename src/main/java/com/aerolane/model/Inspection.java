package com.aerolane.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspections")
public class Inspection {

    public enum EquipmentType {
        XRAY_SCANNER,
        METAL_DETECTOR,
        BODY_SCANNER,
        EXPLOSIVE_TRACE_DETECTOR,
        BAGGAGE_CONVEYOR
    }

    public enum Result {
        PASS,
        FAIL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lane_id")
    private Lane lane;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EquipmentType equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Result result;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, length = 50)
    private String inspectedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Inspection() {
    }

    public Inspection(Lane lane, EquipmentType equipment, Result result, String notes, String inspectedBy) {
        this.lane = lane;
        this.equipment = equipment;
        this.result = result;
        this.notes = notes;
        this.inspectedBy = inspectedBy;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Lane getLane() {
        return lane;
    }

    public EquipmentType getEquipment() {
        return equipment;
    }

    public Result getResult() {
        return result;
    }

    public String getNotes() {
        return notes;
    }

    public String getInspectedBy() {
        return inspectedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
