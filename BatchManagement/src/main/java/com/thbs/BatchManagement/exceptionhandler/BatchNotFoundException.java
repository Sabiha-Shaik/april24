package com.thbs.BatchManagement.exceptionhandler;

public class BatchNotFoundException extends RuntimeException {
	public BatchNotFoundException() {
		super();
	}
	public BatchNotFoundException(String message) {
		super(message);
	}


}

