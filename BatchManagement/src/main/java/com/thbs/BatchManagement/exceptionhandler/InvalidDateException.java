package com.thbs.BatchManagement.exceptionhandler;

public class InvalidDateException extends RuntimeException {
	
		public InvalidDateException() {
			super();
		}
		public InvalidDateException(String message) {
			super(message);
		}


}

