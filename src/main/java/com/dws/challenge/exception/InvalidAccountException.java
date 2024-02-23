package com.dws.challenge.exception;

/**
 * This is user exception class to handle invalid account exception cases
 */
public class InvalidAccountException extends Exception {
    private static final long serialVersionUID = -7947189583145486822L;

    public InvalidAccountException() {
        super();
    }

    public InvalidAccountException(String message) {
        super(message);
    }

}
