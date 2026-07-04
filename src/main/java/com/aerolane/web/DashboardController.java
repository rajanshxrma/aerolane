package com.aerolane.web;

import com.aerolane.model.Lane;
import com.aerolane.service.InspectionService;
import com.aerolane.service.LaneService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final LaneService laneService;
    private final InspectionService inspectionService;
    private final Environment environment;

    public DashboardController(LaneService laneService, InspectionService inspectionService,
            Environment environment) {
        this.laneService = laneService;
        this.inspectionService = inspectionService;
        this.environment = environment;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        model.addAttribute("openLanes", laneService.countByStatus(Lane.Status.OPEN));
        model.addAttribute("closedLanes", laneService.countByStatus(Lane.Status.CLOSED));
        model.addAttribute("maintenanceLanes", laneService.countByStatus(Lane.Status.MAINTENANCE));
        model.addAttribute("todayCount", inspectionService.countSince(startOfDay));
        model.addAttribute("failedTotal",
                inspectionService.countByResult(com.aerolane.model.Inspection.Result.FAIL));
        model.addAttribute("recentInspections", inspectionService.recent());
        return "dashboard";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("ssoEnabled", environment.acceptsProfiles(Profiles.of("sso")));
        return "login";
    }
}
