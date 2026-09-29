package com.cfs.BMS.service;

import com.cfs.BMS.config.AppProperties;
import com.cfs.BMS.dto.BookingRequest;
import com.cfs.BMS.dto.BookingResponse;
import com.cfs.BMS.dto.PageResponse;
import com.cfs.BMS.entity.Booking;
import com.cfs.BMS.entity.Seat;
import com.cfs.BMS.entity.Show;
import com.cfs.BMS.entity.User;
import com.cfs.BMS.enums.BookingStatus;
import com.cfs.BMS.exception.BadRequestException;
import com.cfs.BMS.exception.ConflictException;
import com.cfs.BMS.exception.ForbiddenException;
import com.cfs.BMS.exception.ResourceNotFoundException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.BookingRepository;
import com.cfs.BMS.repository.SeatRepository;
import com.cfs.BMS.repository.ShowRepository;
import com.cfs.BMS.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final UserService userService;
    private final AppProperties props;
    private final Clock clock;

    /**
     * Books seats for the authenticated user.
     * The show row is locked (SELECT ... FOR UPDATE) so concurrent requests for the same show
     * are processed one after another and a seat can never be sold twice.
     */
    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        User user = userService.findEntity(userId);
        Show show = showRepository.findByIdForUpdate(request.showId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.showId()));

        LocalDateTime showStart = LocalDateTime.of(show.getShowDate(), show.getStartTime());
        if (!showStart.isAfter(LocalDateTime.now(clock))) {
            throw new BadRequestException("Bookings are closed - this show has already started");
        }

        List<Long> seatIds = request.seatIds();
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new BadRequestException("Duplicate seat ids in request");
        }
        int max = props.booking().maxSeatsPerBooking();
        if (seatIds.size() > max) {
            throw new BadRequestException("You can book at most " + max + " seats at a time");
        }

        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BadRequestException("One or more seats do not exist");
        }
        Long screenId = show.getScreen().getId();
        for (Seat seat : seats) {
            if (!seat.getScreen().getId().equals(screenId)) {
                throw new BadRequestException("Seat " + seat.getSeatNumber() + " does not belong to this show's screen");
            }
        }

        Set<Long> booked = new HashSet<>(
                bookingRepository.findSeatIdsByShowIdAndStatus(show.getId(), BookingStatus.CONFIRMED));
        List<String> taken = seats.stream()
                .filter(s -> booked.contains(s.getId()))
                .map(Seat::getSeatNumber)
                .sorted()
                .toList();
        if (!taken.isEmpty()) {
            throw new ConflictException("Seats already booked: " + String.join(", ", taken));
        }

        BigDecimal total = seats.stream()
                .map(seat -> ShowService.priceFor(show, seat))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = Booking.builder()
                .bookingReference(generateReference())
                .user(user)
                .show(show)
                .seats(new ArrayList<>(seats))
                .totalPrice(total)
                .status(BookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.now(clock))
                .build();
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} confirmed: user={}, show={}, seats={}, total={}",
                saved.getBookingReference(), userId, show.getId(), seatIds.size(), total);
        return EntityMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, AuthenticatedUser caller) {
        Booking booking = findEntity(id);
        assertCanAccess(booking, caller);
        return EntityMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference, AuthenticatedUser caller) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + reference));
        assertCanAccess(booking, caller);
        return EntityMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getBookingsByUser(Long userId, AuthenticatedUser caller, Pageable pageable) {
        if (!caller.isAdmin() && !caller.id().equals(userId)) {
            throw new ForbiddenException("You can only view your own bookings");
        }
        userService.findEntity(userId);
        return PageResponse.of(bookingRepository.findByUserId(userId, pageable), EntityMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getBookingsByShow(Long showId, Pageable pageable) {
        return PageResponse.of(bookingRepository.findByShowId(showId, pageable), EntityMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getAllBookings(BookingStatus status, Pageable pageable) {
        Page<Booking> page = status == null
                ? bookingRepository.findAll(pageable)
                : bookingRepository.findByStatus(status, pageable);
        return PageResponse.of(page, EntityMapper::toResponse);
    }

    /** Owner or admin may cancel; owners only until the cut-off window before the show starts. */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, AuthenticatedUser caller) {
        Booking booking = findEntity(bookingId);
        assertCanAccess(booking, caller);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime showStart = LocalDateTime.of(booking.getShow().getShowDate(), booking.getShow().getStartTime());
        if (!caller.isAdmin()) {
            int window = props.booking().cancellationWindowHours();
            if (now.plusHours(window).isAfter(showStart)) {
                throw new BadRequestException(
                        "Bookings can only be cancelled at least " + window + " hour(s) before the show starts");
            }
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(now);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} cancelled by user {}", saved.getBookingReference(), caller.id());
        return EntityMapper.toResponse(saved);
    }

    // ---- helpers

    private Booking findEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    private void assertCanAccess(Booking booking, AuthenticatedUser caller) {
        if (!caller.isAdmin() && !booking.getUser().getId().equals(caller.id())) {
            throw new ForbiddenException("You do not have access to this booking");
        }
    }

    private String generateReference() {
        String ref;
        do {
            ref = "BMS" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (bookingRepository.findByBookingReference(ref).isPresent());
        return ref;
    }
}
