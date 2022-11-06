package ru.practicum.shareit.item.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class ItemBadRequestException extends HttpStatusCodeException {

    public ItemBadRequestException(String msg) {
        super(HttpStatus.BAD_REQUEST, msg);
    }

    public ItemBadRequestException(HttpStatus status, String msg) {
        super(status, msg);
    }
}
