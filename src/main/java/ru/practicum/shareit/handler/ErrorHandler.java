package ru.practicum.shareit.handler;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.handler.model.ValidationErrorResponse;
import ru.practicum.shareit.handler.model.Violation;
import ru.practicum.shareit.item.exception.ItemBadRequestException;
import ru.practicum.shareit.user.exception.UserCreationException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    static {
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

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorResponse onConstraintValidationException(
            ConstraintViolationException e) {
        log.error("Handle ConstraintViolationException: {}", e.getMessage());
        ValidationErrorResponse error = new ValidationErrorResponse();
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            error.getViolations().add(
                    new Violation(((PathImpl)violation.getPropertyPath()).getLeafNode().getName(),
                            violation.getMessage()));
        }
        return error;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorResponse onMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.error("Handle MethodArgumentNotValidException: {}", e.getMessage());
        ValidationErrorResponse error = new ValidationErrorResponse();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            error.getViolations().add(
                    new Violation(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return error;
    }

    @ExceptionHandler
    public ResponseEntity<String> handleThrowable(final Throwable e) {
        log.error("Uncatched exception: {}", (Object) e.getStackTrace());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
