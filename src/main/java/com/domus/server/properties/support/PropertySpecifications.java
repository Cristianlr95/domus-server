package com.domus.server.properties.support;

import com.domus.server.properties.entity.PropertyEntity;
import com.domus.server.properties.entity.PropertyStatus;
import com.domus.server.properties.entity.PropertyType;
import org.springframework.data.jpa.domain.Specification;

public final class PropertySpecifications {

    private PropertySpecifications() {
    }

    public static Specification<PropertyEntity> withType(PropertyType type) {
        return (root, query, builder) ->
            type == null ? builder.conjunction() : builder.equal(root.get("type"), type);
    }

    public static Specification<PropertyEntity> withStatus(PropertyStatus status) {
        return (root, query, builder) ->
            status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<PropertyEntity> withBlock(String block) {
        return (root, query, builder) -> {
            if (block == null || block.isBlank()) {
                return builder.conjunction();
            }

            return builder.like(builder.lower(root.get("blockLabel")), "%" + block.trim().toLowerCase() + "%");
        };
    }

    public static Specification<PropertyEntity> search(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                builder.like(builder.lower(root.get("label")), like),
                builder.like(builder.lower(root.get("blockLabel")), like),
                builder.like(builder.lower(root.get("ownerName")), like),
                builder.like(builder.lower(root.get("ownerEmail")), like),
                builder.like(builder.lower(root.get("ownerPhone")), like),
                builder.like(builder.lower(root.get("observations")), like)
            );
        };
    }
}
