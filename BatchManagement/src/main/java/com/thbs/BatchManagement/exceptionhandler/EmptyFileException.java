package com.thbs.BatchManagement.exceptionhandler;

public class EmptyFileException extends RuntimeException{

	
	public EmptyFileException() {
		super();
	}

	public EmptyFileException(String message) {
		super(message);
	}
}


