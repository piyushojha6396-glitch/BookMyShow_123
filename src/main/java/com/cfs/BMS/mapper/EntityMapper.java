package com.cfs.BMS.mapper;

import com.cfs.BMS.dto.*;
import com.cfs.BMS.entity.*;

/**
 * Entity -> response mapping. Call inside a transaction (lazy associations are read here).
 * Responses never expose password hashes or JPA entities.
 */
public final class EntityMapper {

    private EntityMapper() {
    }

    public static UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getRole(), u.isActive(), u.getCreatedAt());
    }

    public static CityResponse toResponse(City c) {
        return new CityResponse(c.getId(), c.getName(), c.getState());
    }

    public static MovieResponse toResponse(Movie m) {
        return new MovieResponse(m.getId(), m.getTitle(), m.getDescription(), m.getGenre(), m.getLanguage(),
                m.getDurationMinutes(), m.getRating(), m.getReleaseDate(), m.getPosterUrl());
    }

    public static TheaterResponse toResponse(Theater t) {
        return new TheaterResponse(t.getId(), t.getName(), t.getAddress(),
                t.getCity().getId(), t.getCity().getName());
    }

    public static ScreenResponse toResponse(Screen s) {
        return new ScreenResponse(s.getId(), s.getName(), s.getTotalSeats(),
                s.getTheater().getId(), s.getTheater().getName());
    }

    public static SeatResponse toResponse(Seat s) {
        return new SeatResponse(s.getId(), s.getSeatNumber(), s.getRow(), s.getCol(),
                s.getSeatType(), s.getScreen().getId());
    }

    public static ShowResponse toResponse(Show s) {
        Screen screen = s.getScreen();
        Theater theater = screen.getTheater();
        City city = theater.getCity();
        return new ShowResponse(s.getId(), s.getMovie().getId(), s.getMovie().getTitle(),
                screen.getId(), screen.getName(), theater.getId(), theater.getName(),
                city.getId(), city.getName(), s.getShowDate(), s.getStartTime(), s.getEndTime(),
                s.getTicketPrice());
    }

    public static BookingResponse toResponse(Booking b) {
        return new BookingResponse(b.getId(), b.getBookingReference(), b.getStatus(), b.getTotalPrice(),
                b.getBookedAt(), b.getCancelledAt(), b.getUser().getId(), b.getUser().getName(),
                toResponse(b.getShow()),
                b.getSeats().stream().map(EntityMapper::toResponse).toList());
    }
}
