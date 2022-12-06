package ru.practicum.shareit.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BookingStatusChangeException extends HttpStatusCodeException {
    public BookingStatusChangeException(String statusText) {
        super(HttpStatus.BAD_REQUEST, statusText);
    }
}
