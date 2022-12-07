package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.handler.model.BookingBadRequest;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.parseState(stateParam);
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                           @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        checkBookingsDates(requestDto); //Validation
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                             @PathVariable Integer bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> setStatus(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                            @PathVariable Integer bookingId,
                                            @RequestParam("approved") Boolean approved) {
        log.info("Patch booking {}, approved={}, user={}", bookingId, approved, userId);
        return bookingClient.setStatus(userId, bookingId, approved);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                                @RequestParam(name = "state", required = false, defaultValue = "ALL")
                                                String stateParam,
                                                @RequestParam(value = "from", defaultValue = "0")
                                                @PositiveOrZero Integer from,
                                                @RequestParam(value = "size", defaultValue = "10")
                                                @Positive Integer size) {
        log.info("Get bookings by owner, userId={}, state={}", userId, stateParam);
        BookingState state = BookingState.parseState(stateParam);
        return bookingClient.getByOwner(userId, state, from, size);
    }

    private void checkBookingsDates(BookItemRequestDto booking) {
        if (!booking.getStart().isBefore(booking.getEnd()))
            throw new BookingBadRequest("Unexpected booking dates.",
                    "Please, check that start booking date is before ending");
    }
}