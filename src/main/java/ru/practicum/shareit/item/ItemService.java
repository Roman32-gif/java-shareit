package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtraInfoDto;

import java.util.List;

public interface ItemService {

    ItemDto addNewItem(Long userId, ItemDto itemDto);

    List<ItemExtraInfoDto> getAllItemsByUserId(Long userId);

    ItemExtraInfoDto getItemById(Long id, Long userId);

    ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto);

    List<ItemDto> searchForItems(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
