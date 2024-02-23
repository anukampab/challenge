package com.dws.challenge.exception;


public class InvalidAccountException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7947189583145486822L;

	public InvalidAccountException(){
		super();
	}
	
	public InvalidAccountException(String message){
		super(message);
	}

}
