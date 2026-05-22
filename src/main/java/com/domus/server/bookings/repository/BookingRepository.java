package com.domus.server.bookings.repository;

import com.domus.server.bookings.entity.BookingEntity;
import com.domus.server.bookings.entity.BookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID>, JpaSpecificationExecutor<BookingEntity> {

    boolean existsByCommonSpace_IdAndBookingDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
        UUID commonSpaceId,
        LocalDate bookingDate,
        List<BookingStatus> statuses,
        LocalTime endTime,
        LocalTime startTime
    );
}
