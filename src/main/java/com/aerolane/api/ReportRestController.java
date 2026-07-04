package com.aerolane.api;

import com.aerolane.service.InspectionService;
import com.aerolane.service.ReportSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportRestController {

    private final InspectionService inspectionService;

    public ReportRestController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping("/summary")
    public ReportSummary summary() {
        return inspectionService.summary();
    }
}
