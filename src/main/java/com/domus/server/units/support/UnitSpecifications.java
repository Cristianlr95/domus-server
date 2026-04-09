package com.domus.server.units.support;

import com.domus.server.units.entity.UnitEntity;
import org.springframework.data.jpa.domain.Specification;

public final class UnitSpecifications {

    private UnitSpecifications() {
    }

    public static Specification<UnitEntity> withActive(Boolean active) {
        return (root, query, builder) ->
            active == null ? builder.conjunction() : builder.equal(root.get("active"), active);
    }

    public static Specification<UnitEntity> search(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                builder.like(builder.lower(root.get("unitCode")), pattern),
                builder.like(builder.lower(root.get("blockLabel")), pattern),
                builder.like(builder.lower(root.get("observations")), pattern)
            );
        };
    }
}
