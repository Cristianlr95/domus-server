package com.domus.server.visits.support;

import com.domus.server.visits.entity.VisitEntity;
import com.domus.server.visits.entity.VisitStatus;
import org.springframework.data.jpa.domain.Specification;

public final class VisitSpecifications {

    private VisitSpecifications() {
    }

    public static Specification<VisitEntity> withStatus(VisitStatus status) {
        return (root, query, builder) ->
            status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<VisitEntity> search(String term) {
        return (root, query, builder) -> {
            if (term == null || term.isBlank()) {
                return builder.conjunction();
            }

            String normalized = "%" + term.trim().toLowerCase() + "%";
            return builder.or(
                builder.like(builder.lower(root.get("visitorName")), normalized),
                builder.like(builder.lower(root.get("visitorDocument")), normalized),
                builder.like(builder.lower(root.get("residentName")), normalized),
                builder.like(builder.lower(root.get("unitLabel")), normalized),
                builder.like(builder.lower(root.get("blockLabel")), normalized)
            );
        };
    }
}
