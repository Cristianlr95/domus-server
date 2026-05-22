package com.domus.server.admin.service;

import com.domus.server.admin.dto.response.AdminDashboardMetricsResponse;
import com.domus.server.admin.dto.response.AdminDashboardResponse;
import com.domus.server.admin.dto.response.AdminRecentActivityResponse;
import com.domus.server.audit.entity.AuditLogEntity;
import com.domus.server.audit.repository.AuditLogRepository;
import com.domus.server.notifications.repository.NotificationRepository;
import com.domus.server.packages.entity.PackageStatus;
import com.domus.server.packages.repository.PackageRepository;
import com.domus.server.parking.repository.ParkingRepository;
import com.domus.server.residents.repository.ResidentRepository;
import com.domus.server.storages.repository.StorageRepository;
import com.domus.server.units.repository.UnitRepository;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.repository.UserRepository;
import com.domus.server.visits.entity.VisitStatus;
import com.domus.server.visits.repository.VisitRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private static final List<PackageStatus> PENDING_PACKAGE_STATUSES = List.of(
        PackageStatus.RECIBIDA,
        PackageStatus.NOTIFICADA
    );

    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final UnitRepository unitRepository;
    private final ParkingRepository parkingRepository;
    private final StorageRepository storageRepository;
    private final VisitRepository visitRepository;
    private final PackageRepository packageRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminDashboardService(
        UserRepository userRepository,
        ResidentRepository residentRepository,
        UnitRepository unitRepository,
        ParkingRepository parkingRepository,
        StorageRepository storageRepository,
        VisitRepository visitRepository,
        PackageRepository packageRepository,
        NotificationRepository notificationRepository,
        AuditLogRepository auditLogRepository
    ) {
        this.userRepository = userRepository;
        this.residentRepository = residentRepository;
        this.unitRepository = unitRepository;
        this.parkingRepository = parkingRepository;
        this.storageRepository = storageRepository;
        this.visitRepository = visitRepository;
        this.packageRepository = packageRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public AdminDashboardResponse getDashboard() {
        return new AdminDashboardResponse(
            getMetrics(),
            getRecentActivity(8),
            Instant.now()
        );
    }

    public List<AdminRecentActivityResponse> getRecentActivity(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));

        return auditLogRepository.findAll(
                PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "occurredAt"))
            )
            .stream()
            .map(this::toActivity)
            .toList();
    }

    private AdminDashboardMetricsResponse getMetrics() {
        return new AdminDashboardMetricsResponse(
            userRepository.count(),
            userRepository.countByActiveTrue(),
            residentRepository.countByActiveTrue(),
            unitRepository.countByActiveTrue(),
            parkingRepository.countByActiveTrue(),
            storageRepository.countByActiveTrue(),
            visitRepository.countByStatus(VisitStatus.PENDIENTE),
            packageRepository.countByStatusIn(PENDING_PACKAGE_STATUSES),
            notificationRepository.countByReadFalse()
        );
    }

    private AdminRecentActivityResponse toActivity(AuditLogEntity auditLog) {
        UserEntity actor = auditLog.getActorUser();
        String actorName = actor == null ? "Sistema" : "%s %s".formatted(actor.getFirstName(), actor.getLastName());
        String actorEmail = actor == null ? null : actor.getEmail();

        return new AdminRecentActivityResponse(
            auditLog.getEntityType(),
            auditLog.getEntityId(),
            auditLog.getAction(),
            auditLog.getSummary(),
            actorName,
            actorEmail,
            auditLog.getOccurredAt()
        );
    }
}
