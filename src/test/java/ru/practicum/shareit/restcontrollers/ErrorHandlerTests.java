package ru.practicum.shareit.restcontrollers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.item.exception.BadCommentException;

import javax.validation.ValidationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void testBadCommentException() {
        BadCommentException exc = new BadCommentException("Message");
        ResponseEntity<String> result = errorHandler.handleCommentNotTrueException(exc);
        assertTrue(result.toString().contains("Message"));
    }
}
