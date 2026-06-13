package com.example.support.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomLogger {

  public Logger getLogger(Class<?> clazz) {
    return LoggerFactory.getLogger(clazz);
  }

  public void info(Class<?> clazz, String message) {
    getLogger(clazz).info(message);
  }

  public void error(Class<?> clazz, String message) {
    getLogger(clazz).error(message);
  }

  public void error(Class<?> clazz, String message, Throwable t) {
    getLogger(clazz).error(message, t);
  }

  public void warn(Class<?> clazz, String message) {
    getLogger(clazz).warn(message);
  }

  public void debug(Class<?> clazz, String message) {
    getLogger(clazz).debug(message);
  }
}
