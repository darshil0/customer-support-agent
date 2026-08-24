package com.example.support;

import com.example.support.logging.CustomLogger;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Centralized exception handler to prevent raw stack trace exposure and internal errors leak. */
@ControllerAdvice
public class GlobalExceptionHandler {

  @Autowired private CustomLogger logger;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
    logger.error(
        GlobalExceptionHandler.class, "Unhandled exception occurred: " + ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "success",
                false,
                "error",
                "An unexpected system error occurred. Please try again later."));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    logger.warn(GlobalExceptionHandler.class, "Invalid request parameter: " + ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "success",
                false,
                "error",
                ex.getMessage() != null ? ex.getMessage() : "Invalid input parameters"));
  }
}
