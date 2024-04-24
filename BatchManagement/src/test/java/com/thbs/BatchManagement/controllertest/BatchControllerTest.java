package com.thbs.BatchManagement.controllertest;

import com.thbs.BatchManagement.controller.BatchController;
import com.thbs.BatchManagement.entity.Batch;
import com.thbs.BatchManagement.entity.EmployeeDTO;
import com.thbs.BatchManagement.exceptionhandler.BatchNotFoundException;
import com.thbs.BatchManagement.exceptionhandler.DuplicateEmployeeException;
import com.thbs.BatchManagement.repository.BatchRepository;
import com.thbs.BatchManagement.service.BatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BatchControllerTest {

    @Mock
    private BatchService batchService;

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private BatchController batchController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    
    @Test
    public void testCreateBatch() {
        Batch batch = new Batch();
        batch.setBatchName("Test Batch");

        when(batchService.createBatch(batch)).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("Batch created successfully"));

        ResponseEntity<?> response = batchController.createBatch(batch);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Batch created successfully", response.getBody());
    }

    
    @Test 
    public void testBulkUpload() throws IOException, ParseException {
        // Mock MultipartFile
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "testdata".getBytes());
        String data = "Test Data";

        // Mock parseExcel method
        List<EmployeeDTO> employees = new ArrayList<>();
        employees.add(new EmployeeDTO((long) 1)); // Example employee
        when(batchService.parseExcel(any(MultipartFile.class))).thenReturn(employees);

        // Mock addEmployeesToBatchFromExcel method
        doNothing().when(batchService).addEmployeesToBatchFromExcel(any(List.class), any(String.class));

        // Perform the bulkUpload operation
        String result = batchController.bulkUpload(file, data);

        // Verify the result
        assertEquals("Batch created successfully", result);
    }
    
    
    @Test
    public void testAddEmployeesToExistingBatches() {
        // Mock data
        Long batchId = 1L;
        List<EmployeeDTO> employees = new ArrayList<>();
        employees.add(new EmployeeDTO((long) 1)); 

        // Mock addEmployeesToExistingBatches method
        doNothing().when(batchService).addEmployeesToExistingBatches(anyLong(), anyList());

        // Perform the addEmployeesToBatch operation
        String result = batchController.addEmployeesToBatch(batchId, employees);

        // Verify the result
        assertEquals("Employees added to batch successfully", result);

        // Verify that batchService.addEmployeesToExistingBatches was called with the correct arguments
        verify(batchService).addEmployeesToExistingBatches(batchId, employees);
    }

    
    @Test
    public void testAddEmployeesToExistingBatchesBulkUpload() throws IOException, BatchNotFoundException, DuplicateEmployeeException {
        // Mock data
        Long batchId = 1L;
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
        List<EmployeeDTO> employees = new ArrayList<>();
        employees.add(new EmployeeDTO((long) 1)); 

        // Mock parseExcel method
        when(batchService.parseExcel(file)).thenReturn(employees);

        // Mock addEmployeesToExistingBatchesFromExcel method
        doNothing().when(batchService).addEmployeesToExistingBatchesFromExcel(anyLong(), anyList());

        // Perform the addEmployeesToExistingBatchBulkUpload operation
        String result = batchController.addEmployeesToExistingBatchBulkUpload(batchId, file);

        // Verify the result
        assertEquals("Employees added to batch successfully", result);

        // Verify that batchService.parseExcel and batchService.addEmployeesToExistingBatchesFromExcel were called with the correct arguments
        verify(batchService).parseExcel(file);
        verify(batchService).addEmployeesToExistingBatchesFromExcel(batchId, employees);
    }
	  
    
    @Test
    public void testGetBatchById() {
        // Mock data
        Long batchId = 1L;
        Batch batch = new Batch();
        batch.setBatchId(batchId);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(batch);

        // Mock getBatchById method
        when(batchService.getBatchById(batchId)).thenReturn(expectedResponse);

        // Perform the getBatchById operation
        ResponseEntity<Object> response = batchController.getBatchById(batchId);

        // Verify the result
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(batch, response.getBody());
    }
    
    
    @Test
    public void testGetBatchByName() {
        // Mock data
        String batchName = "Test Batch";
        Batch batch = new Batch();
        batch.setBatchName(batchName);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(batch);

        // Mock getBatchByName method
        when(batchService.getBatchByName(batchName)).thenReturn(expectedResponse);

        // Perform the getBatchByName operation
        ResponseEntity<Object> response = batchController.getBatchByName(batchName);

        // Verify the result
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(batch, response.getBody());
    }
    
    
    @Test
    public void testGetAllBatches() {
        // Mock data
        List<Batch> batches = new ArrayList<>();
        batches.add(new Batch());
        batches.add(new Batch());

        // Mock getAllBatches method
        when(batchService.getAllBatches()).thenReturn(batches);

        // Perform the getAllBatches operation
        List<Batch> response = batchController.getAllBatches();

        // Verify the result
        assertEquals(2, response.size());
        assertEquals(batches, response);
    }
    

    @Test
    public void testGetAllBatchNames() {
        
        List<String> batchNames = Arrays.asList("Batch1", "Batch2");
        Mockito.when(batchService.getAllBatchNames()).thenReturn(batchNames);

        
        List<String> result = batchController.getAllBatchNames();

        
        assertEquals(batchNames, result);
    }
    
    
    @Test
    public void testGetEmployeesInBatch() {
        Long batchId = 1L;
        List<Long> employeeIds = new ArrayList<>();
        employeeIds.add((long) 1);
        employeeIds.add((long) 2);

        // Mock getEmployeesInBatch method
        when(batchService.getEmployeesInBatch(batchId)).thenReturn(employeeIds);

        // Perform the getEmployeesInBatch operation
        List<Long> response = batchController.getEmployeesInBatch(batchId);

        // Verify the result
        assertEquals(2, response.size());
        assertEquals(employeeIds, response);
    }
    
    
    @Test
    public void testGetEmployeesInBatchByName() {
        String batchName = "TestBatch";
        List<Long> employeeIds = new ArrayList<>();
        employeeIds.add((long) 1);
        employeeIds.add((long) 2);

        // Mock getEmployeesInBatchByName method
        when(batchService.getEmployeesInBatchByName(batchName)).thenReturn(employeeIds);

        // Perform the getEmployeesInBatchByName operation
        List<Long> response = batchController.getEmployeesInBatchByName(batchName);

        // Verify the result
        assertEquals(2, response.size());
        assertEquals(employeeIds, response);
    }
     
    
    @Test
    void testGetAllBatchNamesWithIds() {
        // Mocking service method
        List<Map<String, Object>> result = batchController.getAllBatchNamesWithIds();

        // Verifying return type
        assertNotNull(result);
        assertTrue(result.isEmpty()); 

        
        verify(batchService, times(1)).getAllBatchNamesWithIds();
    }
    
    
    @Test
    public void testDeleteBatchById() {
        Long batchId = 1L;

        // Perform the deleteBatch operation
        batchController.deleteBatch(batchId);

        // Verify that deleteBatchById is called
        verify(batchService, times(1)).deleteBatchById(batchId);
    }
    
    
    @Test
    public void testDeleteBatchByName() {
        String batchName = "TestBatch";

        // Perform the deleteBatch operation
        batchController.deleteBatch(batchName);

        // Verify that deleteBatchByName is called
        verify(batchService, times(1)).deleteBatchByName(batchName);
    }
    
    
    @Test
    public void testDeleteEmployeeFromBatchById() {
        Long batchId = 1L;
        Long employeeId = 123L;

        // Perform the deleteEmployeeFromBatch operation
        batchController.deleteEmployeeFromBatch(batchId, employeeId);

        // Verify that deleteEmployeeFromBatch is called
        verify(batchService, times(1)).deleteEmployeeFromBatch(batchId, employeeId);
    }

    
    @Test
    public void testUpdateEndDate() {
        Long id = 1L;
        Batch batch = new Batch();
        batch.setBatchId(id);
        batch.setEndDate(LocalDate.of(2024, 4, 10)); 

        // Mocking the void method call
        doNothing().when(batchService).updateEndDate(eq(id), any(LocalDate.class));

        String response = batchController.updateEndDate(id, batch);

        verify(batchService, times(1)).updateEndDate(eq(id), any(LocalDate.class));
        assertEquals("EndDate updated successfully", response);
    }

    
    @Test
    public void testUpdateBatchName() {
        Long id = 1L;
        Batch batch = new Batch();
        batch.setBatchId(id);
        batch.setBatchName("New Batch Name");

        // Mocking the void method call
        doNothing().when(batchService).updateBatchName(id, batch.getBatchName());

        String response = batchController.updateBatchName(id, batch);

        verify(batchService, times(1)).updateBatchName(id, batch.getBatchName());
        assertEquals("BatchName updated successfully", response);
    }
    
    
    @Test
    public void testUpdateBatchController() {
        // Mock data
        Long batchId = 1L;
        Batch batch = new Batch();
        batch.setBatchName("TestBatch");
        batch.setBatchDescription("Description");
        

        // Call the method
        String response = batchController.updateBatch(batchId, batch);

        // Verify the response
        assertEquals("Batch details updated successfully", response);

        // Verify interactions
        verify(batchService, times(1)).updateBatch(batchId, batch);  
    }
    
    
    @Test
    public void testDeleteEmployeesFromBatch() { 
        
        Long batchId = 1L;
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);

        // Mock the batchService behavior
        doNothing().when(batchService).deleteEmployeesFromBatch(batchId, employeeIds);

        // Call the controller method
        String result = batchController.deleteEmployeesFromBatch(batchId, employeeIds);

        // Verify the result
        assertEquals("Employees deleted from batch successfully", result);
        verify(batchService, times(1)).deleteEmployeesFromBatch(batchId, employeeIds);
    }
    
    
    
}

    

