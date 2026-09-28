package com.fraudwatch.exception;

/** Request is valid, but conflicts with the current state (e.g. reviewing twice, completing a blocked txn). */
public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String message) { super(message); }
}
