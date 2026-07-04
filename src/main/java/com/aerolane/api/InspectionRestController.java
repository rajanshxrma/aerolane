package com.aerolane.api;

import com.aerolane.api.dto.CreateInspectionRequest;
import com.aerolane.api.dto.InspectionDto;
import com.aerolane.model.Inspection;
import com.aerolane.service.InspectionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/inspections")
public class InspectionRestController {

    private final InspectionService inspectionService;

    public InspectionRestController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping
    public List<InspectionDto> list(@RequestParam(required = false) Inspection.Result result,
            @RequestParam(required = false) Long laneId) {
        List<Inspection> inspections;
        if (result != null) {
            inspections = inspectionService.findByResult(result);
        } else if (laneId != null) {
            inspections = inspectionService.findByLane(laneId);
        } else {
            inspections = inspectionService.findAll();
        }
        return inspections.stream().map(InspectionDto::from).toList();
    }

    @GetMapping("/{id}")
    public InspectionDto get(@PathVariable Long id) {
        return InspectionDto.from(inspectionService.get(id));
    }

    @PostMapping
    public ResponseEntity<InspectionDto> create(@Valid @RequestBody CreateInspectionRequest request,
            Principal principal) {
        Inspection saved = inspectionService.create(request.laneId(), request.equipment(),
                request.result(), request.notes(), principal.getName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(InspectionDto.from(saved));
    }
}
