package com.dws.challenge.exception;

/**
 * This is user exception class to handle insufficient funds exception cases
 */
public class InsufficientFundsException extends Exception {
   
    private static final long serialVersionUID = 4090867331100212991L;

    public InsufficientFundsException() {
        super();
    }

    public InsufficientFundsException(String message) {
        super(message);
    }
}
