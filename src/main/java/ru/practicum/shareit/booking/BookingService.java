package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto create(BookingCreateDto bookingDto, Long userId);

    BookingDto approveBooking(Long bookingId, Long userId, Boolean approved);

    BookingDto getBookingById(Long bookingId, Long userId);

    List<BookingDto> getAllBookingsByUserId(Long userId, String state);

    List<BookingDto> getAllBookingsByOwnerId(Long userId, String state);


}
