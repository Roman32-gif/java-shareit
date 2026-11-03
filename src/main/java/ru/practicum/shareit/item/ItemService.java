package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addNewItem(Long userId, ItemDto itemDto);

    List<ItemDto> getAllItemsByUserId(Long userId);

    ItemDto getItemById(Long id);

    ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto);

    List<ItemDto> searchForItems(String text);
}
