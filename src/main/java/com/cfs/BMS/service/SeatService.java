package com.cfs.BMS.service;

import com.cfs.BMS.dto.SeatRequest;
import com.cfs.BMS.dto.SeatResponse;
import com.cfs.BMS.entity.Screen;
import com.cfs.BMS.entity.Seat;
import com.cfs.BMS.exception.ConflictException;
import com.cfs.BMS.exception.ResourceNotFoundException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.BookingRepository;
import com.cfs.BMS.repository.ScreenRepository;
import com.cfs.BMS.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final ScreenRepository screenRepository;
    private final ScreenService screenService;

    @Transactional
    public SeatResponse addSeat(SeatRequest request) {
        Screen screen = screenService.findEntity(request.screenId());
        String row = request.row().trim().toUpperCase();
        String seatNumber = row + request.col();
        if (seatRepository.existsByScreenIdAndSeatNumberIgnoreCase(screen.getId(), seatNumber)) {
            throw new ConflictException("Seat " + seatNumber + " already exists on this screen");
        }
        Seat seat = seatRepository.save(Seat.builder()
                .seatNumber(seatNumber).row(row).col(request.col())
                .seatType(request.seatType()).screen(screen).build());
        refreshTotalSeats(screen);
        return EntityMapper.toResponse(seat);
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByScreen(Long screenId) {
        screenService.findEntity(screenId);
        return seatRepository.findByScreenIdOrderByRowAscColAsc(screenId).stream()
                .map(EntityMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Seat findEntity(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public SeatResponse getSeatById(Long id) {
        return EntityMapper.toResponse(findEntity(id));
    }

    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = findEntity(id);
        if (bookingRepository.countBySeatId(id) > 0) {
            throw new ConflictException("Seat has bookings and cannot be deleted");
        }
        Screen screen = seat.getScreen();
        seatRepository.delete(seat);
        seatRepository.flush();
        refreshTotalSeats(screen);
    }

    private void refreshTotalSeats(Screen screen) {
        screen.setTotalSeats((int) seatRepository.countByScreenId(screen.getId()));
        screenRepository.save(screen);
    }
}
