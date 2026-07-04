package com.aerolane.api.dto;

import com.aerolane.model.Lane;
import java.time.LocalDateTime;

public record LaneDto(Long id, String name, String terminal, Lane.Status status, LocalDateTime updatedAt) {

    public static LaneDto from(Lane lane) {
        return new LaneDto(lane.getId(), lane.getName(), lane.getTerminal(), lane.getStatus(),
                lane.getUpdatedAt());
    }
}
