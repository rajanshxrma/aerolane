package com.aerolane.repository;

import com.aerolane.model.Inspection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    List<Inspection> findAllByOrderByCreatedAtDesc();

    List<Inspection> findByResultOrderByCreatedAtDesc(Inspection.Result result);

    List<Inspection> findByLaneIdOrderByCreatedAtDesc(Long laneId);

    List<Inspection> findTop10ByOrderByCreatedAtDesc();

    long countByResult(Inspection.Result result);

    long countByCreatedAtAfter(LocalDateTime after);
}
