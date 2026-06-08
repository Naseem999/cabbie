// Purpose: Centralized exception handler for REST API endpoints; converts exceptions to standardized ErrorResponseDTO.
// Notes: Handles UserNotFoundException and generic exceptions; skips handling for SSE subscribe endpoint.

package com.app.cabbie.controller;

import com.app.cabbie.dto.ErrorResponseDTO;
import com.app.cabbie.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    // Purpose: Handles UserNotFoundException by returning a structured error response with NOT_FOUND status.
    // Behavior: Wraps exception message and timestamp into ErrorResponseDTO; returns HTTP 404.
    public ResponseEntity<?> userNotFoundExceptionHandler(UserNotFoundException exception){
        ErrorResponseDTO errorResponse=new ErrorResponseDTO(exception.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    // Purpose: Catches all uncaught exceptions in REST controllers and returns a standardized HTTP 500 error response.
    // Behavior: Skips SSE subscribe endpoint (returns null), wraps other exceptions into ErrorResponseDTO with INTERNAL_SERVER_ERROR status.
    public ResponseEntity<?> genericExceptionHandler(Exception exception, HttpServletRequest request){

        // ← Skip this handler for SSE endpoint — let it handle its own errors
        if (request.getRequestURI().contains("/api/user/notifications/subscribe")) {
            return null; // Spring will handle it naturally
        }

        ErrorResponseDTO errorResponse=new ErrorResponseDTO(exception.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
