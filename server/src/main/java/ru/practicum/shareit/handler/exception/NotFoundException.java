package ru.practicum.shareit.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class NotFoundException extends HttpStatusCodeException {

    public NotFoundException(String msg) {
        super(HttpStatus.NOT_FOUND, msg);
    }
}
