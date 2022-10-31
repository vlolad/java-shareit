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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        Optional<User> owner = Optional.ofNullable(userStorage.get().get(ownerId));
        if (owner.isEmpty()) throw new NotFoundException("Owner ID not found.");
        item.setId(generateId());
        log.debug("Create new Item with id: {}", item.getId());
        itemStorage.get().put(item.getId(), ItemMapper.toItem(item, owner.get()));
        return item;
    }

    public ItemDto patchItem(ItemDto itemDto, Integer ownerId) {
        Optional<Item> item = Optional.ofNullable(itemStorage.get().get(itemDto.getId()));
        if (item.isEmpty()) throw new NotFoundException("Item with ID: " + itemDto.getId() + " not found.");
        if (!Objects.equals(item.get().getOwner().getId(), ownerId))
            throw new ItemBadRequestException(HttpStatus.FORBIDDEN,"Restricted PATCH: user not owner.");
        Item newItem = patchItem(item.get(), itemDto);
        itemStorage.get().replace(newItem.getId(), newItem);
        log.info("Update item (id:{}) successfully", newItem.getId());
        return ItemMapper.toItemDto(newItem);
    }

    public ItemDto getItem(Integer itemId) {
        Optional<Item> item = Optional.ofNullable(itemStorage.get().get(itemId));
        if (item.isPresent()) {
            log.debug("Item with ID: {} found successfully.", itemId);
            return ItemMapper.toItemDto(item.get());
        } else {
            throw new NotFoundException("Item with ID: " + itemId + " not found.");
        }
    }

    public List<ItemDto> getAllItemsByOwner(Integer ownerId) {
        if (!userStorage.get().containsKey(ownerId))
            throw new NotFoundException("Owner (id: " + ownerId + ") not found.");
        log.info("Found owner (id:{}), return items.", ownerId);
        return itemStorage.get().values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) return Collections.emptyList();
        log.debug("Searching: {}", text);
        return itemStorage.get().values().stream()
                .filter(item -> item.getAvailable() && (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    private Item patchItem(Item item, ItemDto newItem) {
        if (!Objects.equals(newItem.getName(), null)) {
            item.setName(newItem.getName());
        }
        if (!Objects.equals(newItem.getDescription(), null)) {
            item.setDescription(newItem.getDescription());
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
