package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        if (itemDto == null) {
            throw new IllegalArgumentException("Введены некорректные данные");
        }
        Item item = ItemMapper.toItem(itemDto);
        User owner = getUserOrThrow(userId);
        item.setOwner(owner);
        Item newItem = itemStorage.create(item);
        log.info("Добавлена новая вещь с id {} пользователем с id {}", newItem.getId(), userId);
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(Long userId) {
        getUserOrThrow(userId);
        log.debug("Получение всех вещей пользователя с id {}", userId);
        return itemStorage.getAllItemsByUserId(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto getItemById(Long id) {
        log.debug("Получение вещи с id {}", id);
        return ItemMapper.toItemDto(getItemOrThrow(id));
    }

    @Override
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
        Item updatedItem = itemStorage.update(item);
        log.info("Вещь с id {} обновлена пользователем с id {}", updatedItem.getId(), userId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> searchForItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        log.debug("Поиск вещей по тексту: {}", text);
        return itemStorage.searchForItems(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private Item getItemOrThrow(Long id) {
        return itemStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));
    }

    private User getUserOrThrow(Long id) {
        return userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }
}
