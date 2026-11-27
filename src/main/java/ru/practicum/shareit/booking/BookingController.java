package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingCreateDto bookingDto, @RequestHeader(USER_ID_HEADER) @Positive Long userId) {
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@Positive @PathVariable Long bookingId, @RequestHeader(USER_ID_HEADER) @Positive Long userId, @RequestParam Boolean approved) {
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@Positive @PathVariable Long bookingId, @RequestHeader(USER_ID_HEADER) @Positive Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsByUserId(@RequestHeader(USER_ID_HEADER) @Positive Long userId, @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsByUserId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwnerId(@RequestHeader(USER_ID_HEADER) @Positive Long userId, @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsByOwnerId(userId, state);
    }

}
