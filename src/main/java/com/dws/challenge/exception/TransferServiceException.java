package com.dws.challenge.exception;

public class TransferServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public TransferServiceException() {
        super();
    }

    public TransferServiceException(String message) {
        super(message);
    }

    public TransferServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public TransferServiceException(Throwable throwable) {
        super(throwable);
    }

}
