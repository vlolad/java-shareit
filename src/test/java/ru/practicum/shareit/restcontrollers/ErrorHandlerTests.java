package ru.practicum.shareit.restcontrollers;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.handler.ErrorHandler;

import javax.validation.ValidationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class ErrorHandlerTests {

    ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void testValidationExceptionHandler() {
        ValidationException exc = new ValidationException("Message");
        String result = errorHandler.handleValidationException(exc);
        assertThat(result, equalTo(exc.getMessage()));
    }
}
