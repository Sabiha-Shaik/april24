package com.thbs.BatchManagement.exceptionhandlertest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.thbs.BatchManagement.exceptionhandler.BatchEmptyException;
import com.thbs.BatchManagement.exceptionhandler.BatchNotFoundException;
import com.thbs.BatchManagement.exceptionhandler.DateTimeException;
import com.thbs.BatchManagement.exceptionhandler.DateTimeParseException;
import com.thbs.BatchManagement.exceptionhandler.DuplicateBatchFoundException;
import com.thbs.BatchManagement.exceptionhandler.DuplicateEmployeeException;
import com.thbs.BatchManagement.exceptionhandler.EmployeeNotFoundException;
import com.thbs.BatchManagement.exceptionhandler.EmptyEmployeesListException;
import com.thbs.BatchManagement.exceptionhandler.EmptyFileException;
import com.thbs.BatchManagement.exceptionhandler.GlobalExceptionHandler;
import com.thbs.BatchManagement.exceptionhandler.InvalidDateException;
import com.thbs.BatchManagement.exceptionhandler.InvalidDateFormatException;
import com.thbs.BatchManagement.exceptionhandler.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;


class  GlobalExceptionHandlerTest {

	
	
    @Test
    void testDuplicateBatchFoundException() {
        // Create an instance of the exception
        DuplicateBatchFoundException ex = new DuplicateBatchFoundException("Duplicate batch found!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.duplicateBatchFoundException(ex);

        // Assert the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Duplicate batch found!", response.getBody());
    }
    
    
    @Test
    void testBatchNotFoundException() {
        // Create an instance of the exception
        BatchNotFoundException ex = new BatchNotFoundException("Batch not found!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.batchNotFoundException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Batch not found!", response.getBody());
    }
    
    
    @Test
    void testInvalidDateException() {
        // Create an instance of the exception
        InvalidDateException ex = new InvalidDateException("Invalid date!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.invalidDateException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Invalid date!", response.getBody());
    }
    
    
    @Test
    void testBatchEmptyException() {
        // Create an instance of the exception
        BatchEmptyException ex = new BatchEmptyException("Batches are not created yet!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.batchEmptyException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Batches are not created yet!", response.getBody());
    }
    
    
    @Test
    void testEmployeeNotFoundException() {
        // Create an instance of the exception
        EmployeeNotFoundException ex = new EmployeeNotFoundException("Employee not found!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.employeeNotFoundException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Employee not found!", response.getBody());
    }
    
    
    @Test
    void testInvalidDateFormatException() {
        // Create an instance of the exception
        InvalidDateFormatException ex = new InvalidDateFormatException("Invalid date format!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.invalidDateFormatException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Invalid date format!", response.getBody());
    }
    
      
    @Test
    void testParseException() {
        // Create an instance of the exception
        ParseException ex = new ParseException("Error parsing date");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.parseException(ex);

        // Assert the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error parsing date", response.getBody());
    }
    
    
    @Test
    void testJsonProcessingException() {
        // Create an instance of the exception
        com.thbs.BatchManagement.exceptionhandler.JsonProcessingException ex =
                new com.thbs.BatchManagement.exceptionhandler.JsonProcessingException("Error processing JSON data");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.jsonProcessingException(ex);

        // Assert the response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error processing JSON data", response.getBody());
    }
    
    
    @Test
    void testEmptyEmployeesListException() {
        // Create an instance of the exception
        EmptyEmployeesListException ex = new EmptyEmployeesListException("Empty employees list!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.emptyEmployeesListException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Empty employees list!", response.getBody());
    }
    
    
    @Test
    void testDuplicateEmployeeException() {
        // Create an instance of the exception
        DuplicateEmployeeException ex = new DuplicateEmployeeException("Duplicate employee!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.duplicateEmployeeException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Duplicate employee!", response.getBody());
    }
    
    
    @Test
    void testEmptyFileException() {
        // Create an instance of the exception
        EmptyFileException ex = new EmptyFileException("Empty file!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.emptyFileException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Empty file!", response.getBody());
    }
    
    
    @Test
    void testHandleDateTimeException() {
        // Create an instance of the exception
        DateTimeException ex = new DateTimeException("Date time exception!");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.handleDateTimeException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Date time exception!", response.getBody());
    }
    
    
    @Test
    void testDateTimeParseException() {
        // Create an instance of the exception
        DateTimeParseException ex = new DateTimeParseException("Error parsing date");

        // Create an instance of the GlobalExceptionHandler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Call the method and capture the response
        ResponseEntity<String> response = handler.dateTimeParseException(ex);

        // Assert the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Error parsing date", response.getBody());
    }
    
    
}
