package com.dws.challenge.exception;

/**
 * This is user exception class to handle invalid amount exception cases
 */
public class InvalidAmountException extends Exception {

    private static final long serialVersionUID = -7947189583145486822L;

    public InvalidAmountException() {
        super();
    }

    public InvalidAmountException(String message) {
        super(message);
    }

}
