package ru.practicum.shareit.restcontrollers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.exception.BookingBadRequest;
import ru.practicum.shareit.booking.exception.BookingCreateException;
import ru.practicum.shareit.booking.exception.BookingStatusChangeException;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.handler.model.ErrorResponse;
import ru.practicum.shareit.item.exception.BadCommentException;
import ru.practicum.shareit.item.exception.ItemBadRequestException;
import ru.practicum.shareit.user.exception.UserCreationException;

import javax.validation.ValidationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class ErrorHandlerTests {

    ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    void testValidationExceptionHandler() {
        ValidationException exc = new ValidationException("Message");
        String result = errorHandler.handleValidationException(exc);
        assertThat(result, equalTo(exc.getMessage()));
    }

    @Test
    void testHandleCommentNotTrueException() {
        ResponseEntity<String> actualHandleCommentNotTrueExceptionResult = errorHandler
                .handleCommentNotTrueException(new BadCommentException("Status Text"));
        assertEquals("400 Status Text", actualHandleCommentNotTrueExceptionResult.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actualHandleCommentNotTrueExceptionResult.getStatusCode());
    }

    @Test
    void testHandleDataIntegrityViolationException() {
        ResponseEntity<String> actualHandleDataIntegrityViolationExceptionResult = errorHandler
                .handleDataIntegrityViolationException(new DataIntegrityViolationException("Msg"));
        assertEquals("Такая запись в базе данных уже есть", actualHandleDataIntegrityViolationExceptionResult.getBody());
        assertEquals(HttpStatus.CONFLICT, actualHandleDataIntegrityViolationExceptionResult.getStatusCode());
    }

    @Test
    void testHandleUserCreationException() {
        ResponseEntity<String> actualHandleUserCreationExceptionResult = errorHandler
                .handleUserCreationException(new UserCreationException("Msg"));
        assertEquals("409 Msg", actualHandleUserCreationExceptionResult.getBody());
        assertEquals(HttpStatus.CONFLICT, actualHandleUserCreationExceptionResult.getStatusCode());
    }

    @Test
    void testHandleItemBadRequestException() {
        ResponseEntity<String> actualHandleItemBadRequestExceptionResult = errorHandler
                .handleItemBadRequestException(new ItemBadRequestException("Msg"));
        assertEquals("400 Msg", actualHandleItemBadRequestExceptionResult.getBody());
        assertEquals(HttpStatus.FORBIDDEN, actualHandleItemBadRequestExceptionResult.getStatusCode());
    }

    @Test
    void testHandleNotFoundException() {
        ResponseEntity<String> actualHandleNotFoundExceptionResult = errorHandler
                .handleNotFoundException(new NotFoundException("Msg"));
        assertEquals("404 Msg", actualHandleNotFoundExceptionResult.getBody());
        assertEquals(HttpStatus.NOT_FOUND, actualHandleNotFoundExceptionResult.getStatusCode());
        assertTrue(actualHandleNotFoundExceptionResult.getHeaders().isEmpty());
    }

    @Test
    void testHandleBookingCreateException() {
        ResponseEntity<String> actualHandleBookingCreateExceptionResult = errorHandler
                .handleBookingCreateException(new BookingCreateException("Msg"));
        assertEquals("400 Msg", actualHandleBookingCreateExceptionResult.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actualHandleBookingCreateExceptionResult.getStatusCode());
    }

    @Test
    void testHandleBookingStatusChangeException() {
        ResponseEntity<String> actualHandleBookingStatusChangeExceptionResult = errorHandler
                .handleBookingStatusChangeException(new BookingStatusChangeException("Status Text"));
        assertEquals("400 Status Text", actualHandleBookingStatusChangeExceptionResult.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, actualHandleBookingStatusChangeExceptionResult.getStatusCode());
    }

    @Test
    void testHandleBookingBadRequest() {
        ErrorResponse actualHandleBookingBadRequestResult = errorHandler
                .handleBookingBadRequest(new BookingBadRequest("Status Text"));
        assertEquals("Only our types allowed.", actualHandleBookingBadRequestResult.getDescription());
        assertEquals("Status Text", actualHandleBookingBadRequestResult.getError());
    }

    @Test
    void testHandleThrowable() {
        ResponseEntity<String> actualHandleThrowableResult = errorHandler.handleThrowable(new Throwable());
        assertNull(actualHandleThrowableResult.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualHandleThrowableResult.getStatusCode());
    }
}
