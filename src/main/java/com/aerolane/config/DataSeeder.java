package com.aerolane.config;

import com.aerolane.model.AppUser;
import com.aerolane.model.Inspection;
import com.aerolane.model.Lane;
import com.aerolane.repository.AppUserRepository;
import com.aerolane.repository.InspectionRepository;
import com.aerolane.repository.LaneRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds demo users, lanes and a spread of inspection records on first boot.
 * Idempotent: does nothing if data already exists, so restarts are safe.
 * Demo credentials are intentionally public — see README.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppUserRepository userRepository;
    private final LaneRepository laneRepository;
    private final InspectionRepository inspectionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository userRepository, LaneRepository laneRepository,
            InspectionRepository inspectionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.laneRepository = laneRepository;
        this.inspectionRepository = inspectionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedLanesAndInspections();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new AppUser("officer", passwordEncoder.encode("officer123"),
                "Officer Diaz", AppUser.Role.OFFICER));
        userRepository.save(new AppUser("supervisor", passwordEncoder.encode("supervisor123"),
                "Supervisor Chen", AppUser.Role.SUPERVISOR));
        userRepository.save(new AppUser("auditor", passwordEncoder.encode("auditor123"),
                "Auditor Patel", AppUser.Role.AUDITOR));
        // SSO-only identity: authenticates through Keycloak, role resolved from this row.
        // Random local password means nobody can form-login as this user.
        userRepository.save(new AppUser("sso.officer", passwordEncoder.encode(UUID.randomUUID().toString()),
                "Officer Reyes (SSO)", AppUser.Role.OFFICER));
        log.info("Seeded demo users");
    }

    private void seedLanesAndInspections() {
        if (laneRepository.count() > 0) {
            return;
        }
        List<Lane> lanes = laneRepository.saveAll(List.of(
                new Lane("Lane 1", "A", Lane.Status.OPEN),
                new Lane("Lane 2", "A", Lane.Status.OPEN),
                new Lane("Lane 3", "A", Lane.Status.MAINTENANCE),
                new Lane("Lane 4", "B", Lane.Status.OPEN),
                new Lane("Lane 5", "B", Lane.Status.CLOSED),
                new Lane("Lane 6", "C", Lane.Status.OPEN)));

        Inspection.EquipmentType[] equipment = Inspection.EquipmentType.values();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 18; i++) {
            Lane lane = lanes.get(i % lanes.size());
            Inspection.EquipmentType type = equipment[i % equipment.length];
            boolean fail = i % 5 == 0;
            Inspection inspection = new Inspection(
                    lane,
                    type,
                    fail ? Inspection.Result.FAIL : Inspection.Result.PASS,
                    fail ? "Calibration drift beyond tolerance, flagged for maintenance." : null,
                    "officer");
            inspection.setCreatedAt(now.minusHours(i * 7L));
            inspectionRepository.save(inspection);
        }
        log.info("Seeded {} lanes and 18 inspections", lanes.size());
    }
}
