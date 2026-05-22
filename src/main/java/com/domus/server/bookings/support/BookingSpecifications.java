package com.domus.server.bookings.support;

import com.domus.server.bookings.entity.BookingEntity;
import com.domus.server.bookings.entity.BookingStatus;
import com.domus.server.bookings.entity.SpaceType;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    public static Specification<BookingEntity> withStatus(BookingStatus status) {
        return (root, query, builder) ->
            status == null ? builder.conjunction() : builder.equal(root.get("status"), status);
    }

    public static Specification<BookingEntity> withSpaceType(SpaceType spaceType) {
        return (root, query, builder) ->
            spaceType == null ? builder.conjunction() : builder.equal(root.get("commonSpace").get("type"), spaceType);
    }

    public static Specification<BookingEntity> fromDate(LocalDate startDate) {
        return (root, query, builder) ->
            startDate == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("bookingDate"), startDate);
    }

    public static Specification<BookingEntity> toDate(LocalDate endDate) {
        return (root, query, builder) ->
            endDate == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("bookingDate"), endDate);
    }

    public static Specification<BookingEntity> search(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String like = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                builder.like(builder.lower(root.get("commonSpace").get("name")), like),
                builder.like(builder.lower(root.get("residentUser").get("firstName")), like),
                builder.like(builder.lower(root.get("residentUser").get("lastName")), like),
                builder.like(builder.lower(root.get("residentUser").get("email")), like),
                builder.like(builder.lower(root.get("observations")), like)
            );
        };
    }
}
