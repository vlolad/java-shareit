package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemBadRequestException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {

    private int id = 0;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemService(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    public ItemDto createItem(ItemDto item, Integer ownerId) {
        User owner = userStorage.get(ownerId);
        if (owner == null) throw new NotFoundException("Owner ID not found.");
        item.setId(generateId());
        log.debug("Create new Item with id: {}", item.getId());
        itemStorage.put(item.getId(), ItemMapper.toItem(item, owner));
        return item;
    }

    public ItemDto patchItem(ItemDto itemDto, Integer ownerId) {
        Item item = itemStorage.get(itemDto.getId());
        if (item == null) throw new NotFoundException("Item with ID: " + itemDto.getId() + " not found.");
        if (!Objects.equals(item.getOwner().getId(), ownerId))
            throw new ItemBadRequestException(HttpStatus.FORBIDDEN,"Restricted PATCH: user not owner.");
        Item newItem = patchItem(item, itemDto);
        itemStorage.replace(newItem.getId(), newItem);
        log.info("Update item (id:{}) successfully", newItem.getId());
        return ItemMapper.toItemDto(newItem);
    }

    public ItemDto getItem(Integer itemId) {
        Item item = itemStorage.get(itemId);
        if (item != null) {
            log.debug("Item with ID: {} found successfully.", itemId);
            return ItemMapper.toItemDto(item);
        } else {
            throw new NotFoundException("Item with ID: " + itemId + " not found.");
        }
    }

    public List<ItemDto> getAllItemsByOwner(Integer ownerId) {
        if (!userStorage.containsKey(ownerId))
            throw new NotFoundException("Owner (id: " + ownerId + ") not found.");
        log.info("Found owner (id:{}), return items.", ownerId);
        return itemStorage.getItemsByOwner(ownerId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text) {
        log.debug("Searching: {}", text);
        return itemStorage.getAll().values().stream()
                .filter(item -> item.getAvailable() && (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    private Item patchItem(Item item, ItemDto newItem) {
        if (!Objects.equals(newItem.getName(), null)) {
            if(!newItem.getName().isBlank()) {
                item.setName(newItem.getName());
            }
        }
        if (!Objects.equals(newItem.getDescription(), null)) {
            if(!newItem.getDescription().isBlank()) {
                item.setDescription(newItem.getDescription());
            }
        }
        if (!Objects.equals(newItem.getAvailable(), null)) {
            item.setAvailable(newItem.getAvailable());
        }
        return item;
    }

    private Integer generateId() {
        log.debug("Generating id...");
        return ++id;
    }
}
