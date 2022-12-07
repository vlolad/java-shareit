package ru.practicum.shareit.handler.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingBadRequest extends RuntimeException {

    private String description;

    public BookingBadRequest(String statusText) {
        super(statusText);
    }

    public BookingBadRequest(String statusText, String description) {
        super(statusText);
        this.description = description;
    }
}
