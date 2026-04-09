package com.domus.server.concierge.service;

import com.domus.server.concierge.dto.response.ConciergeDashboardMetricsResponse;
import com.domus.server.concierge.dto.response.ConciergeDashboardResponse;
import com.domus.server.concierge.dto.response.ConciergeRecentActivityResponse;
import com.domus.server.packages.entity.PackageEntity;
import com.domus.server.packages.entity.PackageStatus;
import com.domus.server.packages.repository.PackageRepository;
import com.domus.server.residents.entity.ResidentEntity;
import com.domus.server.residents.repository.ResidentRepository;
import com.domus.server.units.entity.UnitEntity;
import com.domus.server.units.repository.UnitRepository;
import com.domus.server.visits.entity.VisitEntity;
import com.domus.server.visits.entity.VisitStatus;
import com.domus.server.visits.repository.VisitRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConciergeDashboardService {

    private static final List<PackageStatus> PENDING_PACKAGE_STATUSES = List.of(
        PackageStatus.RECIBIDA,
        PackageStatus.NOTIFICADA
    );

    private final VisitRepository visitRepository;
    private final PackageRepository packageRepository;
    private final ResidentRepository residentRepository;
    private final UnitRepository unitRepository;

    public ConciergeDashboardService(
        VisitRepository visitRepository,
        PackageRepository packageRepository,
        ResidentRepository residentRepository,
        UnitRepository unitRepository
    ) {
        this.visitRepository = visitRepository;
        this.packageRepository = packageRepository;
        this.residentRepository = residentRepository;
        this.unitRepository = unitRepository;
    }

    public ConciergeDashboardResponse getDashboard() {
        return new ConciergeDashboardResponse(
            getMetrics(),
            getRecentActivity(8),
            Instant.now()
        );
    }

    public List<ConciergeRecentActivityResponse> getRecentActivity(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));

        List<ConciergeRecentActivityResponse> activity = buildVisitActivity()
            .stream()
            .limit(3)
            .toList();

        activity = merge(activity, buildPackageActivity().stream().limit(3).toList());
        activity = merge(activity, buildResidentActivity().stream().limit(2).toList());
        activity = merge(activity, buildUnitActivity().stream().limit(2).toList());

        return activity.stream()
            .sorted(Comparator.comparing(ConciergeRecentActivityResponse::occurredAt).reversed())
            .limit(safeLimit)
            .toList();
    }

    private ConciergeDashboardMetricsResponse getMetrics() {
        return new ConciergeDashboardMetricsResponse(
            visitRepository.countByStatus(VisitStatus.INGRESADA),
            visitRepository.countByStatus(VisitStatus.PENDIENTE),
            packageRepository.countByStatusIn(PENDING_PACKAGE_STATUSES),
            residentRepository.countByActiveTrue(),
            unitRepository.countByActiveTrue()
        );
    }

    private List<ConciergeRecentActivityResponse> buildVisitActivity() {
        return visitRepository.findAll(PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt")))
            .stream()
            .map(this::toVisitActivity)
            .toList();
    }

    private List<ConciergeRecentActivityResponse> buildPackageActivity() {
        return packageRepository.findAll(PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "receivedAt")))
            .stream()
            .map(this::toPackageActivity)
            .toList();
    }

    private List<ConciergeRecentActivityResponse> buildResidentActivity() {
        return residentRepository.findAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt")))
            .stream()
            .map(this::toResidentActivity)
            .toList();
    }

    private List<ConciergeRecentActivityResponse> buildUnitActivity() {
        return unitRepository.findAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt")))
            .stream()
            .map(this::toUnitActivity)
            .toList();
    }

    private ConciergeRecentActivityResponse toVisitActivity(VisitEntity visit) {
        String subtitle = joinWithSeparator(
            blankToNull(visit.getResidentName()),
            formatUnit(visit.getBlockLabel(), visit.getUnitLabel())
        );

        return new ConciergeRecentActivityResponse(
            "VISIT",
            "Visita registrada: %s".formatted(visit.getVisitorName()),
            subtitle,
            visit.getStatus().name(),
            visit.getCreatedAt(),
            "/visits/%s".formatted(visit.getId())
        );
    }

    private ConciergeRecentActivityResponse toPackageActivity(PackageEntity packageEntity) {
        String subtitle = joinWithSeparator(
            blankToNull(packageEntity.getResidentName()),
            formatUnit(packageEntity.getBlockLabel(), packageEntity.getUnitLabel())
        );

        return new ConciergeRecentActivityResponse(
            "PACKAGE",
            "Encomienda recibida: %s".formatted(packageEntity.getDescription()),
            subtitle,
            packageEntity.getStatus().name(),
            packageEntity.getReceivedAt(),
            "/packages/%s".formatted(packageEntity.getId())
        );
    }

    private ConciergeRecentActivityResponse toResidentActivity(ResidentEntity resident) {
        String fullName = "%s %s".formatted(resident.getFirstName(), resident.getLastName());
        String subtitle = resident.getUnit() == null
            ? "Sin unidad asociada"
            : formatUnit(resident.getUnit().getBlockLabel(), resident.getUnit().getUnitCode());

        return new ConciergeRecentActivityResponse(
            "RESIDENT",
            "Residente registrado: %s".formatted(fullName),
            subtitle,
            resident.isActive() ? "ACTIVO" : "INACTIVO",
            resident.getCreatedAt(),
            "/residents/%s".formatted(resident.getId())
        );
    }

    private ConciergeRecentActivityResponse toUnitActivity(UnitEntity unit) {
        String subtitle = unit.getFloorNumber() == null
            ? "Unidad sin piso informado"
            : "Piso %s".formatted(unit.getFloorNumber());

        return new ConciergeRecentActivityResponse(
            "UNIT",
            "Unidad registrada: %s".formatted(formatUnit(unit.getBlockLabel(), unit.getUnitCode())),
            subtitle,
            unit.isActive() ? "ACTIVA" : "INACTIVA",
            unit.getCreatedAt(),
            "/units/%s".formatted(unit.getId())
        );
    }

    private List<ConciergeRecentActivityResponse> merge(
        List<ConciergeRecentActivityResponse> first,
        List<ConciergeRecentActivityResponse> second
    ) {
        return java.util.stream.Stream.concat(first.stream(), second.stream()).toList();
    }

    private String formatUnit(String blockLabel, String unitLabel) {
        return joinWithSeparator(blankToNull(blockLabel), blankToNull(unitLabel));
    }

    private String joinWithSeparator(String first, String second) {
        if (first == null) {
            return second == null ? "" : second;
        }

        if (second == null) {
            return first;
        }

        return "%s - %s".formatted(first, second);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
