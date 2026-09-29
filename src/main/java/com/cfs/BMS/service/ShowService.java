package com.cfs.BMS.service;

import com.cfs.BMS.dto.*;
import com.cfs.BMS.entity.Movie;
import com.cfs.BMS.entity.Screen;
import com.cfs.BMS.entity.Seat;
import com.cfs.BMS.entity.Show;
import com.cfs.BMS.enums.BookingStatus;
import com.cfs.BMS.exception.BadRequestException;
import com.cfs.BMS.exception.ConflictException;
import com.cfs.BMS.exception.ResourceNotFoundException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.BookingRepository;
import com.cfs.BMS.repository.SeatRepository;
import com.cfs.BMS.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final MovieService movieService;
    private final ScreenService screenService;
    private final Clock clock;

    @Transactional
    public ShowResponse addShow(ShowRequest request) {
        Movie movie = movieService.findEntity(request.movieId());
        Screen screen = screenService.findEntity(request.screenId());
        LocalTime end = resolveEndTime(request, movie);
        validateSlot(request.showDate(), request.startTime(), end);
        ensureNoOverlap(screen.getId(), request.showDate(), request.startTime(), end, null);

        Show show = Show.builder()
                .movie(movie).screen(screen)
                .showDate(request.showDate())
                .startTime(request.startTime()).endTime(end)
                .ticketPrice(request.ticketPrice().setScale(2, RoundingMode.HALF_UP))
                .build();
        return EntityMapper.toResponse(showRepository.save(show));
    }

    @Transactional(readOnly = true)
    public PageResponse<ShowResponse> getAllShows(Pageable pageable) {
        return PageResponse.of(showRepository.findAll(pageable), EntityMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Show findEntity(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ShowResponse getShowById(Long id) {
        return EntityMapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> getShowByMovie(Long movieId, LocalDate date, Long cityId) {
        movieService.findEntity(movieId);
        return showRepository.findByMovie(movieId, date, cityId).stream().map(EntityMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> getShowByTheater(Long theaterId, LocalDate date) {
        return showRepository.findByTheater(theaterId, date).stream().map(EntityMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> getShowByScreen(Long screenId) {
        screenService.findEntity(screenId);
        return showRepository.findByScreenIdOrderByShowDateAscStartTimeAsc(screenId).stream()
                .map(EntityMapper::toResponse).toList();
    }

    @Transactional
    public ShowResponse updateShow(Long id, ShowRequest request) {
        Show show = findEntity(id);
        Movie movie = movieService.findEntity(request.movieId());
        Screen screen = screenService.findEntity(request.screenId());
        LocalTime end = resolveEndTime(request, movie);

        boolean scheduleChanged = !show.getMovie().getId().equals(movie.getId())
                || !show.getScreen().getId().equals(screen.getId())
                || !show.getShowDate().equals(request.showDate())
                || !show.getStartTime().equals(request.startTime())
                || !show.getEndTime().equals(end);
        if (scheduleChanged) {
            if (bookingRepository.existsByShowIdAndStatus(id, BookingStatus.CONFIRMED)) {
                throw new ConflictException(
                        "Show already has confirmed bookings - only the ticket price can be changed");
            }
            validateSlot(request.showDate(), request.startTime(), end);
            ensureNoOverlap(screen.getId(), request.showDate(), request.startTime(), end, id);
        }
        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.showDate());
        show.setStartTime(request.startTime());
        show.setEndTime(end);
        show.setTicketPrice(request.ticketPrice().setScale(2, RoundingMode.HALF_UP));
        return EntityMapper.toResponse(showRepository.save(show));
    }

    @Transactional
    public void deleteShow(Long id) {
        Show show = findEntity(id);
        if (bookingRepository.existsByShowId(id)) {
            throw new ConflictException("Show has bookings and cannot be deleted");
        }
        showRepository.delete(show);
    }

    /** Every seat of the show's screen with its price and live availability. */
    @Transactional(readOnly = true)
    public List<SeatAvailabilityResponse> getSeatMap(Long showId) {
        Show show = findEntity(showId);
        Set<Long> booked = new HashSet<>(
                bookingRepository.findSeatIdsByShowIdAndStatus(showId, BookingStatus.CONFIRMED));
        return seatRepository.findByScreenIdOrderByRowAscColAsc(show.getScreen().getId()).stream()
                .map(seat -> new SeatAvailabilityResponse(seat.getId(), seat.getSeatNumber(), seat.getRow(),
                        seat.getCol(), seat.getSeatType(), priceFor(show, seat), !booked.contains(seat.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeats(Long showId) {
        Show show = findEntity(showId);
        Set<Long> booked = new HashSet<>(
                bookingRepository.findSeatIdsByShowIdAndStatus(showId, BookingStatus.CONFIRMED));
        return seatRepository.findByScreenIdOrderByRowAscColAsc(show.getScreen().getId()).stream()
                .filter(seat -> !booked.contains(seat.getId()))
                .map(EntityMapper::toResponse)
                .toList();
    }

    public static BigDecimal priceFor(Show show, Seat seat) {
        return show.getTicketPrice()
                .multiply(seat.getSeatType().getPriceMultiplier())
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ---- helpers

    private LocalTime resolveEndTime(ShowRequest request, Movie movie) {
        if (request.endTime() != null) {
            return request.endTime();
        }
        if (movie.getDurationMinutes() == null) {
            throw new BadRequestException("endTime is required because the movie has no duration");
        }
        LocalTime end = request.startTime().plusMinutes(movie.getDurationMinutes());
        if (!end.isAfter(request.startTime())) {
            throw new BadRequestException("Show would run past midnight - pick an earlier start time");
        }
        return end;
    }

    private void validateSlot(LocalDate date, LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("endTime must be after startTime");
        }
        if (!LocalDateTime.of(date, start).isAfter(LocalDateTime.now(clock))) {
            throw new BadRequestException("Show must be scheduled in the future");
        }
    }

    private void ensureNoOverlap(Long screenId, LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        if (showRepository.countOverlapping(screenId, date, start, end, excludeId) > 0) {
            throw new ConflictException("Another show already occupies this screen during that time");
        }
    }
}
