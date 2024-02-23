package com.dws.challenge.exception;
/**
 * This is user exception class to handle lock exception cases
 */
public class LockException extends Exception {
    private static final long serialVersionUID = -4928284032964776178L;

    public LockException(final String message) {
        super(message);
    }
}
