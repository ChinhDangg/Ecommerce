package dev.ecommerce.gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.channels.ClosedChannelException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClosedChannelException.class)
    public ResponseEntity<String> handleClosedChannelException(ClosedChannelException e) {
        return ResponseEntity.internalServerError().body("Server is down");
    }
}
