package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.validate.Create;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> post(@RequestBody @Validated(Create.class) CreateRequestDto request,
                                       @RequestHeader("X-Sharer-User-Id") @Positive Integer userId) {
        log.info("Post item request (length={}) by user id={}", request.getDescription().length(), userId);
        return itemRequestClient.postRequest(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getByUser(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId) {
        log.info("Get all requests for user id={}", userId);
        return itemRequestClient.getByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                         @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Get all requests, by user id={}, from={}, size={}", userId, from, size);
        return itemRequestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") @Positive Integer userId,
                                          @PathVariable("requestId") @Positive Integer requestId) {
        log.info("Get request id={}, by user={}", requestId, userId);
        return itemRequestClient.getById(userId, requestId);
    }
}
