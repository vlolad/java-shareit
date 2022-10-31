package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Map;

public interface ItemStorage {

    Map<Integer, Item> get();
}
