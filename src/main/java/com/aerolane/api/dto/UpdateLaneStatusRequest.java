package com.aerolane.api.dto;

import com.aerolane.model.Lane;
import jakarta.validation.constraints.NotNull;

public record UpdateLaneStatusRequest(
        @NotNull(message = "status is required") Lane.Status status) {
}
