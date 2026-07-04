package com.aerolane.service;

import com.aerolane.model.Inspection;
import com.aerolane.model.Lane;
import com.aerolane.repository.InspectionRepository;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final LaneService laneService;

    public InspectionService(InspectionRepository inspectionRepository, LaneService laneService) {
        this.inspectionRepository = inspectionRepository;
        this.laneService = laneService;
    }

    @Transactional(readOnly = true)
    public List<Inspection> findAll() {
        return inspectionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Inspection> findByResult(Inspection.Result result) {
        return inspectionRepository.findByResultOrderByCreatedAtDesc(result);
    }

    @Transactional(readOnly = true)
    public List<Inspection> findByLane(Long laneId) {
        return inspectionRepository.findByLaneIdOrderByCreatedAtDesc(laneId);
    }

    @Transactional(readOnly = true)
    public List<Inspection> recent() {
        return inspectionRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long countSince(java.time.LocalDateTime after) {
        return inspectionRepository.countByCreatedAtAfter(after);
    }

    @Transactional(readOnly = true)
    public long countByResult(Inspection.Result result) {
        return inspectionRepository.countByResult(result);
    }

    @Transactional(readOnly = true)
    public Inspection get(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inspection " + id + " not found"));
    }

    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR')")
    @Transactional
    public Inspection create(Long laneId, Inspection.EquipmentType equipment, Inspection.Result result,
            String notes, String inspectedBy) {
        Lane lane = laneService.get(laneId);
        Inspection inspection = new Inspection(lane, equipment, result, notes, inspectedBy);
        return inspectionRepository.save(inspection);
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'SUPERVISOR')")
    @Transactional(readOnly = true)
    public ReportSummary summary() {
        List<Inspection> all = inspectionRepository.findAll();
        long total = all.size();
        long failed = all.stream().filter(i -> i.getResult() == Inspection.Result.FAIL).count();
        long passed = total - failed;
        double failRate = total == 0 ? 0.0 : Math.round((failed * 10000.0) / total) / 100.0;

        Map<String, Long> failuresByEquipment = new TreeMap<>();
        all.stream()
                .filter(i -> i.getResult() == Inspection.Result.FAIL)
                .forEach(i -> failuresByEquipment.merge(i.getEquipment().name(), 1L, Long::sum));

        return new ReportSummary(total, passed, failed, failRate, failuresByEquipment);
    }
}
