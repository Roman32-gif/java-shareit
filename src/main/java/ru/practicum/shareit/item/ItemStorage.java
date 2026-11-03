package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {

    Item create(Item item);

    Item update(Item item);

    Optional<Item> findById(Long id);

    List<Item> getAllItemsByUserId(Long userId);

    List<Item> searchForItems(String text);
}
