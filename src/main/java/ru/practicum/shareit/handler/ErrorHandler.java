package ru.practicum.shareit.handler;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exception.BookingBadRequest;
import ru.practicum.shareit.booking.exception.BookingCreateException;
import ru.practicum.shareit.booking.exception.BookingStatusChangeException;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.handler.model.ErrorResponse;
import ru.practicum.shareit.handler.model.ValidationErrorResponse;
import ru.practicum.shareit.handler.model.Violation;
import ru.practicum.shareit.item.exception.BadCommentException;
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
    public ResponseEntity<String> handleCommentNotTrueException(BadCommentException e) {
        log.error("CommentNotTrueException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: {}", e.getMessage());
        return new ResponseEntity<>("Такая запись в базе данных уже есть", HttpStatus.CONFLICT);
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
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleItemBadRequestException(final ItemBadRequestException e) {
        log.error("ItemBadRequestException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleNotFoundException(final NotFoundException e) {
        log.error("NotFoundException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleBookingCreateException(final BookingCreateException e) {
        log.error("BookingCreateException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleBookingStatusChangeException(final BookingStatusChangeException e) {
        log.error("BookingStatusChangeException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingBadRequest(final BookingBadRequest e) {
        log.error("BookingBadRequest: {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), "Only our types allowed.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onConstraintValidationException(
            ConstraintViolationException e) {
        log.error("Handle ConstraintViolationException: {}", e.getMessage());
        ValidationErrorResponse error = new ValidationErrorResponse();
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            error.getViolations().add(
                    new Violation(((PathImpl) violation.getPropertyPath()).getLeafNode().getName(),
                            violation.getMessage()));
        }
        return error;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onMethodArgumentNotValidException(
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
