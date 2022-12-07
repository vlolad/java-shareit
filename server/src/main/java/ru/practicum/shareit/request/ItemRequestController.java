package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShort;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService service;

    @Autowired
    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }

    @PostMapping //postRequest
    @ResponseStatus(HttpStatus.OK)
    public ItemRequestDto create(@RequestBody ItemRequestShort request,
                                 @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("POST-request at /requests");
        return service.create(request, userId);
    }

    @GetMapping //getByUser
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDto> getByUser(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("GET-request at /requests, user: {}", userId);
        return service.getByUser(userId);
    }

    @GetMapping("/all") //getAll
    @ResponseStatus(HttpStatus.OK)
    public List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                       @RequestParam(value = "from") Integer from,
                                       @RequestParam(value = "size") Integer size) {
        log.debug("GET-request at /requests/all?form={}&size={}, user: {}", from, size, userId);
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}") //getById
    @ResponseStatus(HttpStatus.OK)
    public ItemRequestDto getById(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                  @PathVariable("requestId") Integer requestId) {
        log.debug("GET-request at /requests/{}", requestId);
        return service.getById(userId, requestId);
    }
}
