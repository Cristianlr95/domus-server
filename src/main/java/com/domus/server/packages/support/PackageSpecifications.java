package com.domus.server.packages.support;

import com.domus.server.packages.entity.PackageEntity;
import com.domus.server.packages.entity.PackageStatus;
import org.springframework.data.jpa.domain.Specification;

public final class PackageSpecifications {

    private PackageSpecifications() {
    }

    public static Specification<PackageEntity> withStatus(PackageStatus status) {
        return (root, query, builder) ->
            status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<PackageEntity> search(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String pattern = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                builder.like(builder.lower(root.get("description")), pattern),
                builder.like(builder.lower(root.get("residentName")), pattern),
                builder.like(builder.lower(root.get("senderName")), pattern),
                builder.like(builder.lower(root.get("unitLabel")), pattern),
                builder.like(builder.lower(root.get("blockLabel")), pattern)
            );
        };
    }
}
