package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemBadRequestException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.InMemoryUserStorage;
import ru.practicum.shareit.user.User;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    private int id = 0;

    private final InMemoryItemStorage itemStorage;
    private final InMemoryUserStorage userStorage;

    @Autowired
    public ItemController(InMemoryUserStorage userStorage, InMemoryItemStorage itemStorage) {
        this.userStorage = userStorage;
        this.itemStorage = itemStorage;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestBody @Valid ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.debug("POST-request at /items");
        Optional<User> owner = Optional.ofNullable(userStorage.get().get(ownerId));
        if (owner.isEmpty()) throw new NotFoundException("Owner ID not found.");
        Item item = new Item(
                generateId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner.get()
        );
        itemDto.setId(item.getId());
        itemStorage.get().put(item.getId(), item);
        log.debug("Create new Item with id: {}", item.getId());
        return itemDto;
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto patchItem(@RequestBody ItemDto itemDto,
                             @RequestHeader("X-Sharer-User-Id") Integer ownerId,
                             @PathVariable Integer itemId) {
        log.debug("PATCH-request at /items/{}", itemId);
        Item item = itemStorage.get().get(itemId);
        if (!Objects.equals(item.getOwner().getId(), ownerId))
            throw new ItemBadRequestException(HttpStatus.FORBIDDEN,"Restricted PATCH: user not owner.");
        item.patchItem(itemDto);
        return ItemMapper.toItemDto(item);
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto getItem(@PathVariable Integer itemId) {
        log.debug("GET-request at /items/{}", itemId);
        Optional<Item> item = Optional.ofNullable(itemStorage.get().get(itemId));
        if (item.isPresent()) {
            log.debug("Item with ID: {} found successfully.", itemId);
            return ItemMapper.toItemDto(item.get());
        } else {
            throw new NotFoundException("Item with ID: " + itemId + " not found.");
        }
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.debug("GET-request at /items. Owner ID: {}", ownerId);
        return itemStorage.get().values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> searchItems(@RequestParam("text") String text) {
        log.debug("GET-request at /items/search?text={}", text);
        if (text.isBlank()) return Collections.emptyList();
        return itemStorage.get().values().stream()
                .filter(item -> item.getAvailable() && (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    private Integer generateId() {
        log.debug("Generating id...");
        return ++id;
    }
}
