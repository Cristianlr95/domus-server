package com.domus.server.bookings.service;

import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.service.AuditLogService;
import com.domus.server.bookings.dto.request.CreateBookingRequest;
import com.domus.server.bookings.dto.request.UpdateBookingStatusRequest;
import com.domus.server.bookings.dto.response.BookingResponse;
import com.domus.server.bookings.dto.response.CommonSpaceResponse;
import com.domus.server.bookings.entity.BookingEntity;
import com.domus.server.bookings.entity.BookingStatus;
import com.domus.server.bookings.entity.CommonSpaceEntity;
import com.domus.server.bookings.entity.SpaceType;
import com.domus.server.bookings.mapper.BookingMapper;
import com.domus.server.bookings.repository.BookingRepository;
import com.domus.server.bookings.repository.CommonSpaceRepository;
import com.domus.server.bookings.support.BookingSpecifications;
import com.domus.server.common.exception.ResourceNotFoundException;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(BookingStatus.RESERVADA, BookingStatus.CONFIRMADA);

    private final BookingRepository bookingRepository;
    private final CommonSpaceRepository commonSpaceRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final AuditLogService auditLogService;

    public BookingService(
        BookingRepository bookingRepository,
        CommonSpaceRepository commonSpaceRepository,
        UserRepository userRepository,
        BookingMapper bookingMapper,
        AuditLogService auditLogService
    ) {
        this.bookingRepository = bookingRepository;
        this.commonSpaceRepository = commonSpaceRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<CommonSpaceResponse> listSpaces() {
        return commonSpaceRepository.findAllByActiveTrueOrderByNameAsc()
            .stream()
            .map(bookingMapper::toResponse)
            .toList();
    }

    public BookingResponse create(CreateBookingRequest request, UUID actorUserId) {
        CommonSpaceEntity space = commonSpaceRepository.findById(request.commonSpaceId())
            .filter(CommonSpaceEntity::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Common space not found."));
        UserEntity residentUser = userRepository.findById(actorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking user not found."));

        validateSchedule(request);
        validateCapacity(space, request.guestCount());
        validateAvailability(space, request, null);

        BookingEntity booking = new BookingEntity();
        booking.setId(UUID.randomUUID());
        booking.setCommonSpace(space);
        booking.setResidentUser(residentUser);
        booking.setStatus(BookingStatus.RESERVADA);
        booking.setBookingDate(request.bookingDate());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setGuestCount(request.guestCount());
        booking.setObservations(blankToNull(request.observations()));

        BookingEntity savedBooking = bookingRepository.save(booking);
        BookingResponse response = bookingMapper.toResponse(savedBooking);
        auditLogService.record(
            actorUserId,
            "BOOKING",
            savedBooking.getId().toString(),
            AuditAction.CREATE,
            "Booking for " + space.getName() + " created.",
            null,
            response,
            java.util.Map.of("commonSpaceId", space.getId(), "bookingDate", savedBooking.getBookingDate())
        );
        return response;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> list(
        BookingStatus status,
        SpaceType spaceType,
        LocalDate startDate,
        LocalDate endDate,
        String search
    ) {
        Specification<BookingEntity> specification = BookingSpecifications.withStatus(status)
            .and(BookingSpecifications.withSpaceType(spaceType))
            .and(BookingSpecifications.fromDate(startDate))
            .and(BookingSpecifications.toDate(endDate))
            .and(BookingSpecifications.search(search));

        return bookingRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "bookingDate").and(Sort.by("startTime")))
            .stream()
            .map(bookingMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(UUID id) {
        return bookingMapper.toResponse(getBooking(id));
    }

    public BookingResponse updateStatus(UUID id, UpdateBookingStatusRequest request, UUID actorUserId) {
        BookingEntity booking = getBooking(id);
        BookingResponse previousState = bookingMapper.toResponse(booking);
        BookingStatus previousStatus = booking.getStatus();

        if (request.status() == BookingStatus.DISPONIBLE) {
            throw new IllegalArgumentException("A booking cannot be moved to available status.");
        }

        booking.setStatus(request.status());
        if (request.status() == BookingStatus.CONFIRMADA) {
            booking.setApprovedByUser(userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found.")));
        }
        if (request.observations() != null) {
            booking.setObservations(blankToNull(request.observations()));
        }

        BookingEntity savedBooking = bookingRepository.save(booking);
        BookingResponse response = bookingMapper.toResponse(savedBooking);
        auditLogService.record(
            actorUserId,
            "BOOKING",
            savedBooking.getId().toString(),
            AuditAction.STATUS_CHANGE,
            "Booking status changed from " + previousStatus + " to " + request.status() + ".",
            previousState,
            response,
            java.util.Map.of("previousStatus", previousStatus, "newStatus", request.status())
        );
        return response;
    }

    public BookingResponse cancel(UUID id, UUID actorUserId) {
        return updateStatus(id, new UpdateBookingStatusRequest(BookingStatus.CANCELADA, null), actorUserId);
    }

    private BookingEntity getBooking(UUID id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
    }

    private void validateSchedule(CreateBookingRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("Booking start time must be before end time.");
        }
    }

    private void validateCapacity(CommonSpaceEntity space, Integer guestCount) {
        if (guestCount != null && guestCount > space.getCapacity()) {
            throw new IllegalArgumentException("Guest count exceeds common space capacity.");
        }
    }

    private void validateAvailability(CommonSpaceEntity space, CreateBookingRequest request, UUID currentId) {
        boolean overlaps = bookingRepository.existsByCommonSpace_IdAndBookingDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            space.getId(),
            request.bookingDate(),
            BLOCKING_STATUSES,
            request.endTime(),
            request.startTime()
        );

        if (overlaps && currentId == null) {
            throw new IllegalArgumentException("The selected common space is already booked for that schedule.");
        }
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
