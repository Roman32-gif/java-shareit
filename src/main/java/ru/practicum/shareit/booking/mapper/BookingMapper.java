package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingHalfDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public final class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .item(ItemMapper.toItemDto(booking.getItem()))
                .build();
    }


    public static Booking toBooking(BookingCreateDto bookingDto, Item item, User booker) {
        if (bookingDto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }

    public BookingHalfDto toShortDto(Booking booking) {
        return BookingHalfDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}
