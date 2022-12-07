package ru.practicum.shareit.booking.exception;

public class BookingBadRequest extends RuntimeException {

    public BookingBadRequest(String statusText) {
        super(statusText);
    }
}
