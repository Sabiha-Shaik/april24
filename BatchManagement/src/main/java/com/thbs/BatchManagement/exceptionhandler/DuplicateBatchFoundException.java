package com.thbs.BatchManagement.exceptionhandler;

public class DuplicateBatchFoundException extends RuntimeException {

	public DuplicateBatchFoundException() {
		super();
	}
	public DuplicateBatchFoundException(String message) {
		super(message);
	}

}

