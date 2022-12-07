package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.validate.Create;
import ru.practicum.shareit.validate.Update;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collections;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Validated(Create.class) CreateItemRequest itemDto,
                                         @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Create item, item_name={}, userId={}", itemDto.getName(), userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> edit(@RequestBody @Validated(Update.class) CreateItemRequest itemDto,
                                       @RequestHeader("X-Sharer-User-Id") Integer userId,
                                       @PathVariable Integer itemId) {
        log.info("Patch item id={}, by userId={}", itemId, userId);
        return itemClient.editItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@PathVariable Integer itemId,
                                      @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Get item id={}, userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                         @RequestParam(value = "from", defaultValue = "0", required = false)
                                         @PositiveOrZero Integer from,
                                         @RequestParam(value = "size", defaultValue = "10", required = false)
                                         @Positive Integer size) {
        log.info("Get all items for userId={}, from={}, size={}", userId, from, size);
        return itemClient.getAllItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam("text") String text,
                                         @RequestParam(value = "from", defaultValue = "0")
                                         @PositiveOrZero Integer from,
                                         @RequestParam(value = "size", defaultValue = "10")
                                         @Positive Integer size,
                                         @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Search items, text={}, userId={}", text, userId);
        if (text.isBlank()) return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        return itemClient.search(userId, from, size, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> sendComment(@PathVariable Integer itemId,
                                              @RequestBody @Valid CreateCommentDto comment,
                                              @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.info("Post comment to item id={}, author id={}, text length: {}", itemId, userId, comment.getText().length());
        return itemClient.sendComment(userId, itemId, comment);
    }
}
