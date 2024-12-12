package com.bank.app.account_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleAllExceptions() {
        Exception exception = new Exception("Test exception");
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<String> response = globalExceptionHandler.handleAllExceptions(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred: Test exception", response.getBody());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgumentException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input: Invalid argument", response.getBody());
    }

    @Test
    void testHandleAccountNotFoundException() {
        AccountNotFoundException exception = new AccountNotFoundException("Account not found");
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<String> response = globalExceptionHandler.handleAccountNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account not found: Account not found", response.getBody());
    }

    @Test
    void testHandleInsufficientFundsException() {
        InsufficientFundsException exception = new InsufficientFundsException("Insufficient funds");
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<String> response = globalExceptionHandler.handleInsufficientFundsException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient funds: Insufficient funds", response.getBody());
    }
}
