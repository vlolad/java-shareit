package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Integer, Item> itemStorage = new HashMap<>();

    public Map<Integer, Item> get() {
        return itemStorage;
    }
}
