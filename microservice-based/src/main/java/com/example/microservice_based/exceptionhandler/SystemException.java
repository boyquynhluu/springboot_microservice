package com.example.microservice_based.exceptionhandler;

@SuppressWarnings("serial")
public class SystemException extends Exception {

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
