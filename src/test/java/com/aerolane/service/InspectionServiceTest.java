package com.aerolane.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aerolane.model.Inspection;
import com.aerolane.model.Lane;
import com.aerolane.repository.InspectionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private LaneService laneService;

    @InjectMocks
    private InspectionService inspectionService;

    @Test
    void createLooksUpLaneAndSaves() {
        Lane lane = new Lane("Lane 1", "A", Lane.Status.OPEN);
        when(laneService.get(1L)).thenReturn(lane);
        when(inspectionRepository.save(any(Inspection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Inspection created = inspectionService.create(1L, Inspection.EquipmentType.XRAY_SCANNER,
                Inspection.Result.PASS, "routine check", "officer");

        ArgumentCaptor<Inspection> captor = ArgumentCaptor.forClass(Inspection.class);
        verify(inspectionRepository).save(captor.capture());
        assertThat(captor.getValue().getLane()).isSameAs(lane);
        assertThat(created.getInspectedBy()).isEqualTo("officer");
        assertThat(created.getResult()).isEqualTo(Inspection.Result.PASS);
    }

    @Test
    void summaryComputesFailRateAndGroupsFailuresByEquipment() {
        Lane lane = new Lane("Lane 1", "A", Lane.Status.OPEN);
        List<Inspection> data = List.of(
                new Inspection(lane, Inspection.EquipmentType.XRAY_SCANNER, Inspection.Result.FAIL, null, "officer"),
                new Inspection(lane, Inspection.EquipmentType.XRAY_SCANNER, Inspection.Result.FAIL, null, "officer"),
                new Inspection(lane, Inspection.EquipmentType.METAL_DETECTOR, Inspection.Result.PASS, null, "officer"),
                new Inspection(lane, Inspection.EquipmentType.BODY_SCANNER, Inspection.Result.PASS, null, "officer"),
                new Inspection(lane, Inspection.EquipmentType.BAGGAGE_CONVEYOR, Inspection.Result.PASS, null, "officer"));
        when(inspectionRepository.findAll()).thenReturn(data);

        ReportSummary summary = inspectionService.summary();

        assertThat(summary.totalInspections()).isEqualTo(5);
        assertThat(summary.passed()).isEqualTo(3);
        assertThat(summary.failed()).isEqualTo(2);
        assertThat(summary.failRatePercent()).isEqualTo(40.0);
        assertThat(summary.failuresByEquipment())
                .containsEntry("XRAY_SCANNER", 2L)
                .doesNotContainKey("METAL_DETECTOR");
    }

    @Test
    void summaryOfNothingIsAllZeroes() {
        when(inspectionRepository.findAll()).thenReturn(List.of());

        ReportSummary summary = inspectionService.summary();

        assertThat(summary.totalInspections()).isZero();
        assertThat(summary.failRatePercent()).isZero();
        assertThat(summary.failuresByEquipment()).isEmpty();
    }
}
