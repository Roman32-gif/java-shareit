package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(BookingCreateDto dto, Long userId) {
        User booker = getUserOrThrow(userId);
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + dto.getItemId() + " не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new IllegalArgumentException("Вещь с id " + item.getId() + " недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нельзя бронировать собственную вещь");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }

        Booking saved = bookingRepository.save(BookingMapper.toBooking(dto, item, booker));
        log.info("Создано бронирование с id {} пользователем с id {}", saved.getId(), userId);
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long bookingId, Long userId, Boolean approved) {

        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтверждать бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        log.info("Бронирование с id {} получило статус {}", bookingId, saved.getStatus());
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        getUserOrThrow(userId);
        Booking booking = getBookingOrThrow(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование с id " + bookingId + " не найдено");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingsByUserId(Long userId, String state) {
        getUserOrThrow(userId);
        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByBooker_Id(userId, SORT_BY_START_DESC);
            case CURRENT -> bookingRepository
                    .findByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, SORT_BY_START_DESC);
            case PAST -> bookingRepository.findByBooker_IdAndEndBefore(userId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartAfter(userId, now, SORT_BY_START_DESC);
            case WAITING -> bookingRepository
                    .findByBooker_IdAndStatus(userId, BookingStatus.WAITING, SORT_BY_START_DESC);
            case REJECTED -> bookingRepository
                    .findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> getAllBookingsByOwnerId(Long userId, String state) {
        getUserOrThrow(userId);
        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByItem_Owner_Id(userId, SORT_BY_START_DESC);
            case CURRENT -> bookingRepository
                    .findByItem_Owner_IdAndStartBeforeAndEndAfter(userId, now, now, SORT_BY_START_DESC);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndBefore(userId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartAfter(userId, now, SORT_BY_START_DESC);
            case WAITING -> bookingRepository
                    .findByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, SORT_BY_START_DESC);
            case REJECTED -> bookingRepository
                    .findByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
    }

   private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));
    }
}
