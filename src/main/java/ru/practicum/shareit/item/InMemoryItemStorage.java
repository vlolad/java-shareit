package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Integer, Item> itemStorage = new HashMap<>();
    private final Map<Integer, List<Item>> userItemIndex = new LinkedHashMap<>();

    public Map<Integer, Item> getAll() {
        log.info("Get all storage");
        return new HashMap<>(itemStorage);
    }

    public void put(Integer key, Item value) {
        log.info("Put key={}: {}", key, value);
        itemStorage.put(key, value);
        final List<Item> items = userItemIndex.computeIfAbsent(value.getOwner().getId(), c -> new ArrayList<>());
        items.add(value);
        userItemIndex.put(key, items);
    }

    public Item get(Integer key) {
        log.info("Get key={}", key);
        return itemStorage.get(key);
    }

    public void replace(Integer key, Item value) {
        log.info("Replace on key={}: {}", key, value);
        Item prevItem = itemStorage.replace(key, value);
        final List<Item> items = userItemIndex.computeIfAbsent(value.getOwner().getId(), c -> new ArrayList<>());
        if (prevItem != null) {
            items.remove(prevItem);
        }
        items.add(value);
        userItemIndex.put(key, items);
    }

    public Item remove(Integer key) {
        log.info("Remove key={}", key);
        Item oldItem = itemStorage.remove(key);
        if (oldItem != null) {
            final List<Item> items = userItemIndex.computeIfAbsent(oldItem.getOwner().getId(), c -> new ArrayList<>());
            items.remove(oldItem);
            userItemIndex.put(key, items);
        }
        return oldItem;
    }

    public boolean containsKey(Integer key) {
        log.info("ContainsKey key={}", key);
        return itemStorage.containsKey(key);
    }

    public List<Item> getItemsByOwner(Integer ownerId) {
        log.info("Get items by ownerId={}", ownerId);
        return new ArrayList<>(userItemIndex.get(ownerId));
    }
}
