package com.thrift4j.client.exception;


public class NoAvailableTransportException extends Exception {

  public NoAvailableTransportException(String message, String className) {
    this(message, className, null);
  }

  public NoAvailableTransportException(String message, String className, Throwable cause) {
    super(message + " class name is " + className, cause);
  }
}
