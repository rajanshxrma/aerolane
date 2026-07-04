package com.aerolane.service;

import com.aerolane.model.Lane;
import com.aerolane.repository.LaneRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LaneService {

    private final LaneRepository laneRepository;

    public LaneService(LaneRepository laneRepository) {
        this.laneRepository = laneRepository;
    }

    @Transactional(readOnly = true)
    public List<Lane> findAll() {
        return laneRepository.findAllByOrderByTerminalAscNameAsc();
    }

    @Transactional(readOnly = true)
    public Lane get(Long id) {
        return laneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lane " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public long countByStatus(Lane.Status status) {
        return laneRepository.countByStatus(status);
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    @Transactional
    public Lane changeStatus(Long id, Lane.Status newStatus) {
        Lane lane = get(id);
        lane.changeStatus(newStatus);
        return laneRepository.save(lane);
    }
}
