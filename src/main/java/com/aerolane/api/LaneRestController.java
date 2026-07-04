package com.aerolane.api;

import com.aerolane.api.dto.LaneDto;
import com.aerolane.api.dto.UpdateLaneStatusRequest;
import com.aerolane.service.LaneService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lanes")
public class LaneRestController {

    private final LaneService laneService;

    public LaneRestController(LaneService laneService) {
        this.laneService = laneService;
    }

    @GetMapping
    public List<LaneDto> list() {
        return laneService.findAll().stream().map(LaneDto::from).toList();
    }

    @GetMapping("/{id}")
    public LaneDto get(@PathVariable Long id) {
        return LaneDto.from(laneService.get(id));
    }

    @PatchMapping("/{id}")
    public LaneDto changeStatus(@PathVariable Long id, @Valid @RequestBody UpdateLaneStatusRequest request) {
        return LaneDto.from(laneService.changeStatus(id, request.status()));
    }
}
