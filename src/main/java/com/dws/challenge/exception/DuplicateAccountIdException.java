package com.dws.challenge.exception;

/**
 * This is user exception class to handle duplicate accountId exception cases
 */
public class DuplicateAccountIdException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DuplicateAccountIdException(String message) {
        super(message);
    }
}
