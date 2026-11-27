package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingHalfDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtraInfoDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        if (itemDto == null) {
            throw new IllegalArgumentException("Введены некорректные данные");
        }
        Item item = ItemMapper.toItem(itemDto);
        User owner = getUserOrThrow(userId);
        item.setOwner(owner);
        Item newItem = itemRepository.save(item);
        log.info("Добавлена новая вещь с id {} пользователем с id {}", newItem.getId(), userId);
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public List<ItemExtraInfoDto> getAllItemsByUserId(Long userId) {
        getUserOrThrow(userId);
        log.debug("Получение всех вещей пользователя с id {}", userId);
        return itemRepository.findByOwner_Id(userId).stream()
                .map(item -> toDetailsDto(item, true))
                .toList();
    }

    @Override
    public ItemExtraInfoDto getItemById(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        return toDetailsDto(item, item.getOwner().getId().equals(userId));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto) {
        if (itemDto == null) {
            throw new IllegalArgumentException("Введены некорректные данные");
        }
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь с id " + userId + " не является владельцем вещи с id " + itemId);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        Item updatedItem = itemRepository.save(item);
        log.info("Вещь с id {} обновлена пользователем с id {}", updatedItem.getId(), userId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> searchForItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        log.debug("Поиск вещей по тексту: {}", text);
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        boolean hasRented = bookingRepository.existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!hasRented) {
            throw new IllegalArgumentException(
                    "Пользователь с id " + userId + " не брал вещь с id " + itemId + " в аренду");
        }

        Comment saved = commentRepository.save(ItemMapper.toComment(commentDto, item, author));
        log.info("Пользователь с id {} оставил комментарий к вещи с id {}", userId, itemId);
        return ItemMapper.toCommentDto(saved);
    }

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    private ItemExtraInfoDto toDetailsDto(Item item, boolean withBookings) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = withBookings
                ? bookingRepository.findByItem_IdAndStatus(item.getId(), BookingStatus.APPROVED)
                : List.of();

        BookingHalfDto last = bookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toShortDto)
                .orElse(null);
        BookingHalfDto next = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toShortDto)
                .orElse(null);

        List<CommentDto> comments = commentRepository.findByItemId(item.getId()).stream()
                .map(ItemMapper::toCommentDto)
                .toList();
        return ItemMapper.toItemExtraInfoDto(item, last, next, comments);
    }
}
