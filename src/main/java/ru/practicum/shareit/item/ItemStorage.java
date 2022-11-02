package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemStorage {

    void put(Integer key, Item value);

    Item get(Integer key);

    Map<Integer, Item> getAll();

    void replace(Integer key, Item value);

    Item remove(Integer key);

    boolean containsKey(Integer key);

    List<Item> getItemsByOwner(Integer ownerId);
}
