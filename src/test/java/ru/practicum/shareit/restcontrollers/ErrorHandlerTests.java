package ru.practicum.shareit.restcontrollers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.handler.ErrorHandler;

import javax.validation.ValidationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
public class ErrorHandlerTests {

    @Autowired
    ErrorHandler errorHandler;

    @Test
    void testValidationExceptionHandler() {
        ValidationException exc = new ValidationException("Message");
        String result = errorHandler.handleValidationException(exc);
        assertThat(result, equalTo(exc.getMessage()));
    }
}
