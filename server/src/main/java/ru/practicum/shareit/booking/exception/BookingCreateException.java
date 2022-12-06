package ru.practicum.shareit.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BookingCreateException extends HttpStatusCodeException {

    public BookingCreateException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
    }

    public BookingCreateException(String msg) {
        super(HttpStatus.BAD_REQUEST, msg);
    }
}
