package com.aerolane.web;

import com.aerolane.model.Inspection;
import com.aerolane.model.Lane;
import com.aerolane.service.InspectionService;
import com.aerolane.service.LaneService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inspections")
public class InspectionController {

    private final InspectionService inspectionService;
    private final LaneService laneService;

    public InspectionController(InspectionService inspectionService, LaneService laneService) {
        this.inspectionService = inspectionService;
        this.laneService = laneService;
    }

    @ModelAttribute("lanes")
    public List<Lane> lanes() {
        return laneService.findAll();
    }

    @ModelAttribute("equipmentTypes")
    public Inspection.EquipmentType[] equipmentTypes() {
        return Inspection.EquipmentType.values();
    }

    @ModelAttribute("results")
    public Inspection.Result[] results() {
        return Inspection.Result.values();
    }

    @GetMapping
    public String list(@RequestParam(required = false) Inspection.Result result,
            @RequestParam(required = false) Long laneId, Model model) {
        List<Inspection> inspections;
        if (result != null) {
            inspections = inspectionService.findByResult(result);
        } else if (laneId != null) {
            inspections = inspectionService.findByLane(laneId);
        } else {
            inspections = inspectionService.findAll();
        }
        model.addAttribute("inspections", inspections);
        model.addAttribute("activeResult", result);
        model.addAttribute("activeLaneId", laneId);
        return "inspections/list";
    }

    @GetMapping("/new")
    public String newInspection(Model model) {
        model.addAttribute("form", new InspectionForm());
        return "inspections/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") InspectionForm form, BindingResult bindingResult,
            Principal principal, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "inspections/new";
        }
        inspectionService.create(form.getLaneId(), form.getEquipment(), form.getResult(),
                form.getNotes(), principal.getName());
        redirectAttributes.addFlashAttribute("flash", "Inspection logged.");
        return "redirect:/inspections";
    }
}
