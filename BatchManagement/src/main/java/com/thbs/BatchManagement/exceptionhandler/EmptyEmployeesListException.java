package com.thbs.BatchManagement.exceptionhandler;

public class EmptyEmployeesListException  extends RuntimeException{

	
	public EmptyEmployeesListException() {
		super();
	}

	public EmptyEmployeesListException(String message) {
		super(message);
	}
}


