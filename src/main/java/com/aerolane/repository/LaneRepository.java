package com.aerolane.repository;

import com.aerolane.model.Lane;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaneRepository extends JpaRepository<Lane, Long> {

    List<Lane> findAllByOrderByTerminalAscNameAsc();

    long countByStatus(Lane.Status status);
}
