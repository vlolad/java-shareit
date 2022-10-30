package ru.practicum.shareit.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class UserCreationException extends HttpStatusCodeException {

    public UserCreationException(String msg) {
        super(HttpStatus.CONFLICT, msg);
    }

     public UserCreationException(HttpStatus status, String msg) {
        super(status, msg);
     }
}
