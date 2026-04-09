package com.domus.server.audit.support;

import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.entity.AuditLogEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLogEntity> withActorUserId(UUID actorUserId) {
        return (root, query, builder) -> actorUserId == null
            ? builder.conjunction()
            : builder.equal(root.join("actorUser").get("id"), actorUserId);
    }

    public static Specification<AuditLogEntity> withEntityType(String entityType) {
        return (root, query, builder) -> entityType == null || entityType.isBlank()
            ? builder.conjunction()
            : builder.equal(builder.upper(root.get("entityType")), entityType.trim().toUpperCase());
    }

    public static Specification<AuditLogEntity> withAction(AuditAction action) {
        return (root, query, builder) -> action == null
            ? builder.conjunction()
            : builder.equal(root.get("action"), action);
    }

    public static Specification<AuditLogEntity> occurredFrom(Instant from) {
        return (root, query, builder) -> from == null
            ? builder.conjunction()
            : builder.greaterThanOrEqualTo(root.get("occurredAt"), from);
    }

    public static Specification<AuditLogEntity> occurredTo(Instant to) {
        return (root, query, builder) -> to == null
            ? builder.conjunction()
            : builder.lessThanOrEqualTo(root.get("occurredAt"), to);
    }

    public static Specification<AuditLogEntity> search(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String term = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                builder.like(builder.lower(root.get("summary")), term),
                builder.like(builder.lower(root.get("entityType")), term),
                builder.like(builder.lower(root.get("entityId")), term)
            );
        };
    }
}
