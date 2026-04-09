package com.domus.server.residents.support;

import com.domus.server.residents.entity.ResidentEntity;
import org.springframework.data.jpa.domain.Specification;

public final class ResidentSpecifications {

    private ResidentSpecifications() {
    }

    public static Specification<ResidentEntity> withActive(Boolean active) {
        return (root, query, builder) ->
            active == null ? builder.conjunction() : builder.equal(root.get("active"), active);
    }

    public static Specification<ResidentEntity> search(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                builder.like(builder.lower(root.get("firstName")), pattern),
                builder.like(builder.lower(root.get("lastName")), pattern),
                builder.like(builder.lower(root.get("documentNumber")), pattern),
                builder.like(builder.lower(root.get("email")), pattern),
                builder.like(builder.lower(root.get("unitLabel")), pattern),
                builder.like(builder.lower(root.get("blockLabel")), pattern)
            );
        };
    }
}
