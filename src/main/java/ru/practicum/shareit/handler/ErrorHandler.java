package ru.practicum.shareit.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.exception.ItemBadRequestException;
import ru.practicum.shareit.user.exception.UserCreationException;

import javax.validation.ValidationException;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    static{
        Locale.setDefault(new Locale("en"));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(final ValidationException e) {
        log.error("Validation error: {}", e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUserCreationException(final UserCreationException e) {
        log.error("UserCreationException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleItemBadRequestException(final ItemBadRequestException e) {
        log.error("ItemBadRequestException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleNotFoundException(final NotFoundException e) {
        log.error("NotFoundException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }
}
