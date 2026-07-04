package com.aerolane.web;

import com.aerolane.model.Lane;
import com.aerolane.service.LaneService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lanes")
public class LaneController {

    private final LaneService laneService;

    public LaneController(LaneService laneService) {
        this.laneService = laneService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("lanes", laneService.findAll());
        model.addAttribute("statuses", Lane.Status.values());
        return "lanes/list";
    }

    @PostMapping("/manage/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam Lane.Status status,
            RedirectAttributes redirectAttributes) {
        Lane lane = laneService.changeStatus(id, status);
        redirectAttributes.addFlashAttribute("flash",
                lane.getName() + " set to " + lane.getStatus().name().toLowerCase() + ".");
        return "redirect:/lanes";
    }
}
