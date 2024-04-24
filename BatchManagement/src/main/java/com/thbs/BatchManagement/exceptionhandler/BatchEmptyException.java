package com.thbs.BatchManagement.exceptionhandler;

public class BatchEmptyException extends RuntimeException {
	public BatchEmptyException() {
		super();
	}
	public BatchEmptyException(String message) {
		super(message);
	} 
}



