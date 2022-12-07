package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.exception.BookingBadRequest;

public enum StateMode {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static StateMode parseState(String line) {
        StateMode state;
        try {
            state = StateMode.valueOf(line.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BookingBadRequest("Unknown state: " + line);
        }
        return state;
    }
}
