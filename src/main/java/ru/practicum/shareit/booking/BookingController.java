package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.exception.BookingCreateException;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@RequestBody @Valid BookingRequest request,
                                    @RequestHeader("X-Sharer-User-Id") Integer requesterId) {
        log.debug("POST-request at /bookings: {}", request);
        if (request.getStart().isAfter(request.getEnd()))
            throw new BookingCreateException("Booking start is after the end.");
        return bookingService.create(request, requesterId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto setStatusByOwner(@PathVariable Integer bookingId,
                                       @RequestParam("approved") Boolean approved,
                                       @RequestHeader("X-Sharer-User-Id") Integer ownerId) {
        log.debug("POST-request at /bookings/{}&approved={}", bookingId, approved);
        return bookingService.changeStatusByOwner(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@PathVariable Integer bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("GET-request at /bookings/{}", bookingId);
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAll(@RequestParam (name = "state", required = false, defaultValue = "ALL")
                                            String state,
            @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("GET-request at /bookings");
        return bookingService.getAllByUser(state, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestParam (name = "state", required = false, defaultValue = "ALL")
                                             String state,
            @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("GET-request at /bookings/owner");
        return bookingService.getAllByUserOwner(state, userId);
    }
}
