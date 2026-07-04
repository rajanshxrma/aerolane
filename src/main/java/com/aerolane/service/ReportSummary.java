package com.aerolane.service;

import java.util.Map;

public record ReportSummary(
        long totalInspections,
        long passed,
        long failed,
        double failRatePercent,
        Map<String, Long> failuresByEquipment) {
}
