package com.thbs.BatchManagement.exceptionhandler;

public class DuplicateEmployeeException extends RuntimeException{

	
	public DuplicateEmployeeException() {
		super();
	}

	public DuplicateEmployeeException(String message) {
		super(message);
	}
}


