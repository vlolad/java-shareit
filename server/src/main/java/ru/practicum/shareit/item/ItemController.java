package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping //createItem
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestBody CreateItemRequest itemDto,
                          @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.debug("POST-request at /items");
        return itemService.create(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}") //editItem
    @ResponseStatus(HttpStatus.OK)
    public ItemDto patch(@RequestBody CreateItemRequest itemDto,
                         @RequestHeader("X-Sharer-User-Id") Integer ownerId,
                         @PathVariable Integer itemId) {
        log.debug("PATCH-request at /items/{}", itemId);
        itemDto.setId(itemId);
        return itemService.patchItem(itemDto, ownerId);
    }

    @GetMapping("/{itemId}") //getItem
    @ResponseStatus(HttpStatus.OK)
    public ItemDto get(@PathVariable Integer itemId,
                       @RequestHeader("X-Sharer-User-Id") Integer requesterId) {
        log.debug("GET-request at /items/{}", itemId);
        return itemService.getItem(itemId, requesterId);
    }

    @GetMapping //getAllItems
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") Integer ownerId,
                                @RequestParam(value = "from") Integer from,
                                @RequestParam(value = "size") Integer size) {
        log.debug("GET-request at /items. Owner ID: {}", ownerId);
        return itemService.getAllByOwner(ownerId, from, size);
    }

    @GetMapping("/search") //search
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> search(@RequestParam("text") String text,
                                @RequestParam(value = "from") Integer from,
                                @RequestParam(value = "size") Integer size) {
        log.debug("GET-request at /items/search?text={}", text);
        if (text.isBlank()) return Collections.emptyList();
        return itemService.search(text, from, size);
    }

    @PostMapping("/{itemId}/comment") //sendComment
    @ResponseStatus(HttpStatus.OK)
    public CommentDto createComment(@PathVariable Integer itemId,
                                    @RequestBody CommentDto commentText,
                                    @RequestHeader("X-Sharer-User-Id") Integer authorId) {
        log.debug("POST-request at /items/{}/comment", itemId);
        commentText.setItemId(itemId);
        return itemService.createComment(commentText, authorId);
    }
}
