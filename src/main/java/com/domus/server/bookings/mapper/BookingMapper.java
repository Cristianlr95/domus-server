package com.domus.server.bookings.mapper;

import com.domus.server.bookings.dto.response.BookingResponse;
import com.domus.server.bookings.dto.response.BookingUserSummaryResponse;
import com.domus.server.bookings.dto.response.CommonSpaceResponse;
import com.domus.server.bookings.entity.BookingEntity;
import com.domus.server.bookings.entity.CommonSpaceEntity;
import com.domus.server.residents.entity.ResidentEntity;
import com.domus.server.residents.repository.ResidentRepository;
import com.domus.server.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final ResidentRepository residentRepository;

    public BookingMapper(ResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    public CommonSpaceResponse toResponse(CommonSpaceEntity space) {
        return new CommonSpaceResponse(
            space.getId(),
            space.getName(),
            space.getType(),
            space.getCapacity(),
            space.getDescription(),
            space.getImageUrl(),
            space.isActive()
        );
    }

    public BookingResponse toResponse(BookingEntity booking) {
        UserEntity residentUser = booking.getResidentUser();
        ResidentEntity resident = residentRepository.findByLinkedUser_Id(residentUser.getId()).orElse(null);

        return new BookingResponse(
            booking.getId(),
            booking.getCommonSpace().getId(),
            booking.getCommonSpace().getName(),
            booking.getCommonSpace().getType(),
            residentUser.getId(),
            residentName(residentUser),
            resident != null && resident.getUnit() != null ? resident.getUnit().getUnitCode() : null,
            resident != null && resident.getUnit() != null ? resident.getUnit().getBlockLabel() : null,
            booking.getStatus(),
            booking.getBookingDate(),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getGuestCount(),
            booking.getObservations(),
            booking.getCreatedAt(),
            booking.getUpdatedAt(),
            toUserSummary(residentUser, resident),
            toUserSummary(booking.getApprovedByUser(), null)
        );
    }

    private BookingUserSummaryResponse toUserSummary(UserEntity user, ResidentEntity resident) {
        if (user == null) {
            return null;
        }

        return new BookingUserSummaryResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            resident == null ? null : resident.getPhone()
        );
    }

    private String residentName(UserEntity user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
