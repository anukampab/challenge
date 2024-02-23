package com.dws.challenge.exception;


/**
 * This is user exception class to handle account not found cases
 */
public class AccountNotFoundException extends Exception {

	private static final long serialVersionUID = -6601745803948002536L;

	public AccountNotFoundException(){
		super();
	}
	
	public AccountNotFoundException(String message){
		super(message);
	}

}
