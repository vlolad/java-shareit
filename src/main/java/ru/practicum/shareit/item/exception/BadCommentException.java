package ru.practicum.shareit.item.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BadCommentException extends HttpStatusCodeException {

    public BadCommentException(String statusText) {
        super(HttpStatus.BAD_REQUEST, statusText);
    }

    public BadCommentException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
    }
}
