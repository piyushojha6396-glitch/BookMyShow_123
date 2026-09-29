package com.cfs.BMS.service;

import com.cfs.BMS.dto.PageResponse;
import com.cfs.BMS.dto.ScreenRequest;
import com.cfs.BMS.dto.ScreenResponse;
import com.cfs.BMS.dto.ScreenUpdateRequest;
import com.cfs.BMS.entity.Screen;
import com.cfs.BMS.entity.Seat;
import com.cfs.BMS.entity.Theater;
import com.cfs.BMS.enums.SeatType;
import com.cfs.BMS.exception.BadRequestException;
import com.cfs.BMS.exception.ConflictException;
import com.cfs.BMS.exception.ResourceNotFoundException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.ScreenRepository;
import com.cfs.BMS.repository.SeatRepository;
import com.cfs.BMS.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final TheaterService theaterService;

    /** Creates the screen and generates its whole seat grid in one transaction. */
    @Transactional
    public ScreenResponse addScreen(ScreenRequest request) {
        Theater theater = theaterService.findEntity(request.theaterId());
        String name = request.name().trim();
        if (screenRepository.existsByTheaterIdAndNameIgnoreCase(theater.getId(), name)) {
            throw new ConflictException("Screen '" + name + "' already exists in this theater");
        }
        int rows = request.rows();
        int perRow = request.seatsPerRow();
        int premiumRows = request.premiumRows() == null ? 0 : request.premiumRows();
        int vipRows = request.vipRows() == null ? 0 : request.vipRows();
        if (premiumRows + vipRows > rows) {
            throw new BadRequestException("premiumRows + vipRows cannot exceed total rows");
        }

        Screen screen = screenRepository.save(Screen.builder()
                .name(name).theater(theater).totalSeats(rows * perRow).build());

        List<Seat> seats = new ArrayList<>(rows * perRow);
        for (int r = 0; r < rows; r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            int fromBack = rows - 1 - r;
            SeatType type = fromBack < vipRows ? SeatType.VIP
                    : fromBack < vipRows + premiumRows ? SeatType.PREMIUM
                    : SeatType.REGULAR;
            for (int c = 1; c <= perRow; c++) {
                seats.add(Seat.builder()
                        .seatNumber(rowLabel + c).row(rowLabel).col(c)
                        .seatType(type).screen(screen).build());
            }
        }
        seatRepository.saveAll(seats);
        return EntityMapper.toResponse(screen);
    }

    @Transactional(readOnly = true)
    public PageResponse<ScreenResponse> getAllScreens(Pageable pageable) {
        return PageResponse.of(screenRepository.findAll(pageable), EntityMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Screen findEntity(Long id) {
        return screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ScreenResponse getScreenById(Long id) {
        return EntityMapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ScreenResponse> getScreenByTheater(Long theaterId) {
        theaterService.findEntity(theaterId);
        return screenRepository.findByTheaterIdOrderByName(theaterId).stream()
                .map(EntityMapper::toResponse).toList();
    }

    @Transactional
    public ScreenResponse renameScreen(Long id, ScreenUpdateRequest request) {
        Screen screen = findEntity(id);
        String name = request.name().trim();
        if (screenRepository.existsByTheaterIdAndNameIgnoreCaseAndIdNot(screen.getTheater().getId(), name, id)) {
            throw new ConflictException("Screen '" + name + "' already exists in this theater");
        }
        screen.setName(name);
        return EntityMapper.toResponse(screenRepository.save(screen));
    }

    @Transactional
    public void deleteScreen(Long id) {
        Screen screen = findEntity(id);
        if (showRepository.existsByScreenId(id)) {
            throw new ConflictException("Screen has shows scheduled and cannot be deleted");
        }
        seatRepository.deleteAllByScreenId(id);
        screenRepository.delete(screen);
    }
}
