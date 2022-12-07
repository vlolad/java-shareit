package ru.practicum.shareit.handler.model;

public class BookingBadRequest extends RuntimeException {

    public BookingBadRequest(String statusText) {
        super(statusText);
    }
}
