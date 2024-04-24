package com.thbs.BatchManagement.servicetest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thbs.BatchManagement.entity.Batch;
import com.thbs.BatchManagement.entity.EmployeeDTO;
import com.thbs.BatchManagement.exceptionhandler.BatchEmptyException;
import com.thbs.BatchManagement.exceptionhandler.BatchNotFoundException;
import com.thbs.BatchManagement.exceptionhandler.DuplicateBatchFoundException;
import com.thbs.BatchManagement.exceptionhandler.DuplicateEmployeeException;
import com.thbs.BatchManagement.exceptionhandler.EmployeeNotFoundException;
import com.thbs.BatchManagement.exceptionhandler.EmptyEmployeesListException;
import com.thbs.BatchManagement.exceptionhandler.ParseException;
import com.thbs.BatchManagement.repository.BatchRepository;
import com.thbs.BatchManagement.service.BatchService;
import static org.junit.jupiter.api.Assertions.assertThrows;


import org.apache.poi.ss.usermodel.Workbook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BatchServiceTest {

	@Mock
	private BatchRepository batchRepository;

	@InjectMocks
	private BatchService batchService;

	
	@Mock
    private Workbook mockWorkbook;
	
	private Map<Long, List<Long>> batchEmployeeMap = new HashMap<>();
	
	@Mock
    private RestTemplate restTemplate;
	
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	

	@Test
	public void testCreateBatch() {
		Batch batch = new Batch();
		batch.setBatchName("Test Batch");

		when(batchRepository.existsByBatchName(batch.getBatchName())).thenReturn(false);
		when(batchRepository.save(batch)).thenReturn(batch);

		assertDoesNotThrow(() -> batchService.createBatch(batch));
	}

	
    @Test
    public void testAddEmployeesToBatchFromExcel() throws IOException, ParseException, java.text.ParseException {
        List<EmployeeDTO> employees = new ArrayList<>();
        employees.add(new EmployeeDTO((long) 1));

        String data = "{\"batchName\": \"Test Batch\", \"duration\": 10, \"startDate\": \"2024-04-08\", \"endDate\": \"2024-04-18\", \"batchSize\": 20}";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(data);

        // Mocking the behavior of the batchRepository.existsByBatchName method
        when(batchRepository.existsByBatchName("Test Batch")).thenReturn(false);

        // Call the method to be tested
        batchService.addEmployeesToBatchFromExcel(employees, data);

        // Verify that batchRepository.save was called once
        verify(batchRepository, times(1)).save(any());

        
    }

    
    @Test
    public void testAddEmployeesToBatchFromExcelDuplicateBatch() throws IOException, ParseException {
        List<EmployeeDTO> employees = new ArrayList<>();
        employees.add(new EmployeeDTO((long) 1)); 

        String data = "{\"batchName\": \"Test Batch\", \"duration\": 10, \"startDate\": \"2024-04-08\", \"endDate\": \"2024-04-18\", \"batchSize\": 20}";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(data);

        // Mocking the behavior of the batchRepository.existsByBatchName method to return true (indicating a duplicate batch)
        when(batchRepository.existsByBatchName("Test Batch")).thenReturn(true);

        // Verify that the DuplicateBatchFoundException is thrown
        assertThrows(DuplicateBatchFoundException.class, () ->
        batchService.addEmployeesToBatchFromExcel(employees, data));

        // Verify that batchRepository.save was never called
        verify(batchRepository, never()).save(any());
    }

    
//    @Test
//    void testAddEmployeesToExistingBatches() {
//        // Mock data
//        Long batchId = 1L;
//        List<EmployeeDTO> employees = new ArrayList<>();
//        employees.add(new EmployeeDTO(101L));
//        employees.add(new EmployeeDTO(102L));
//
//        Batch batch = new Batch();
//        batch.setEmployeeId(new ArrayList<>());
//
//        when(batchRepository.findById(batchId)).thenReturn(java.util.Optional.of(batch));
//
//        // Call the method
//        batchService.addEmployeesToExistingBatches(batchId, employees);
//
//        // Verify that batchRepository.save() is called once
//        verify(batchRepository, times(1)).save(batch);
//    }
    
    
//    @Test
//    void testGetBatchEmployeeMap() {
//        // Mock data
//        Long batchId = 1L;
//        List<Long> employeeIds = new ArrayList<>();
//        employeeIds.add(101L);
//        employeeIds.add(102L);
//
//        // Mock batchEmployeeMap
//        Map<Long, List<Long>> batchEmployeeMap = new HashMap<>();
//        batchEmployeeMap.put(batchId, employeeIds);
//
//        // Call the method
//        String jsonData = batchService.getBatchEmployeeMap(batchEmployeeMap);
//
//        // Expected result
//        String expectedJsonData = "{\"batchId\":1,\"userIds\":[101,102]}";
//
//        // Verify the result
//        assertEquals(expectedJsonData, jsonData);
//    }
    
    
    @Test
    void testPostBatchEmployeeMap() {
        // Mock data
        String requestData = "{\"batchId\": 1,\"userIds\": [101, 102]}";
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);

        // Mock the behavior of restTemplate.postForEntity() using any() matcher
        when(restTemplate.postForEntity(any(String.class), any(), eq(String.class))).thenReturn(mockResponse);

        // Call the method  
        String response = batchService.postBatchEmployeeMap(requestData);

        // Verify the response
        assertEquals("Success", response);
    }
    
    
	@Test
	public void testGetAllBatchNames() {
	        
	        Batch batch1 = new Batch();
	        batch1.setBatchName("Batch1");
	
	        Batch batch2 = new Batch();
	        batch2.setBatchName("Batch2");
	
	        List<Batch> batches = Arrays.asList(batch1, batch2);
	        Mockito.when(batchRepository.findAll()).thenReturn(batches);
	
	        
	        List<String> result = batchService.getAllBatchNames();
	
	       
	        assertEquals(Arrays.asList("Batch1", "Batch2"), result);
	 }
	
	
     @Test
	 public void testGetAllBatchNamesEmpty() {
		List<Batch> batches = new ArrayList<>();

		when(batchRepository.findAll()).thenReturn(batches);

		Exception exception = assertThrows(BatchEmptyException.class, () -> batchService.getAllBatchNames());

		assertEquals("Batches are not created yet", exception.getMessage());

		verify(batchRepository, times(1)).findAll();
	 }


	@Test
	public void testGetBatchById() {
		Long batchId = 1L;
		Batch batch = new Batch();
		batch.setBatchId(batchId);

		when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

		ResponseEntity<Object> responseEntity = batchService.getBatchById(batchId);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(batch, responseEntity.getBody());

		verify(batchRepository, times(1)).findById(batchId);
	}
 
	
	@Test
	public void testGetBatchByIdNotFound() {
		Long batchId = 999L;

		when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

		Exception exception = assertThrows(BatchNotFoundException.class, () -> batchService.getBatchById(batchId));

		assertEquals("Batch not found with id " + batchId, exception.getMessage());

		verify(batchRepository, times(1)).findById(batchId);
	}

	
	@Test
	public void testGetBatchByName() {
		String batchName = "TestBatch";
		Batch batch = new Batch();
		batch.setBatchName(batchName);

		when(batchRepository.findByBatchName(batchName)).thenReturn(Optional.of(batch));

		ResponseEntity<Object> responseEntity = batchService.getBatchByName(batchName);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(batch, responseEntity.getBody());

		verify(batchRepository, times(1)).findByBatchName(batchName);
	}

	
	@Test
	public void testGetBatchByNameNotFound() {
		String batchName = "NonExistentBatch";

		when(batchRepository.findByBatchName(batchName)).thenReturn(Optional.empty());

		Exception exception = assertThrows(BatchNotFoundException.class, () -> batchService.getBatchByName(batchName));

		assertEquals("Batch not found with name " + batchName, exception.getMessage());

		verify(batchRepository, times(1)).findByBatchName(batchName);
	}

	
	@Test
	public void testGetAllBatches() {
		List<Batch> batches = new ArrayList<>();
		batches.add(new Batch());
		batches.add(new Batch());
		batches.add(new Batch());

		when(batchRepository.findAll()).thenReturn(batches);

		List<Batch> result = batchService.getAllBatches();

		assertEquals(batches.size(), result.size());
		assertEquals(batches.get(0), result.get(0));
		assertEquals(batches.get(1), result.get(1));
		assertEquals(batches.get(2), result.get(2));

		verify(batchRepository, times(1)).findAll();
	}

	
	@Test
	public void testGetAllBatchesEmpty() {
		List<Batch> batches = new ArrayList<>();

		when(batchRepository.findAll()).thenReturn(batches);

		Exception exception = assertThrows(BatchEmptyException.class, () -> batchService.getAllBatches());

		assertEquals("Batches are not created yet", exception.getMessage());

		verify(batchRepository, times(1)).findAll();
	}

	
	@Test
	public void testGetEmployeesInBatch() {
		Long batchId = 1L;

		Batch batch = new Batch();
		batch.setBatchId(batchId);
		List<Long> employeeIds = new ArrayList<>();
		employeeIds.add((long) 1);
		employeeIds.add((long) 2);
		batch.setEmployeeId(employeeIds);

		when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

		List<Long> result = batchService.getEmployeesInBatch(batchId);

		assertEquals(employeeIds, result);

		verify(batchRepository, times(1)).findById(batchId);
	}

	
	@Test
	public void testGetEmployeesInBatchBatchEmpty() {
		Long batchId = 1L;

		Batch batch = new Batch();
		batch.setBatchId(batchId);
		batch.setEmployeeId(new ArrayList<>());

		when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

		Exception exception = assertThrows(BatchEmptyException.class, () -> batchService.getEmployeesInBatch(batchId));

		assertEquals("No employees found in batch with id " + batchId, exception.getMessage());

		verify(batchRepository, times(1)).findById(batchId);
	}

	
	@Test
	public void testGetEmployeesInBatchBatchNotFound() {
		Long batchId = 1L;

		when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

		Exception exception = assertThrows(BatchNotFoundException.class,
				() -> batchService.getEmployeesInBatch(batchId));

		assertEquals("Batch with id " + batchId + " not found.", exception.getMessage());

		verify(batchRepository, times(1)).findById(batchId);
	}

	
	@Test
	public void testGetEmployeesInBatchByName() {
		String batchName = "TestBatch";

		Batch batch = new Batch();
		batch.setBatchName(batchName);
		List<Long> employeeIds = new ArrayList<>();
		employeeIds.add((long) 1);
		employeeIds.add((long) 2);
		batch.setEmployeeId(employeeIds);

		when(batchRepository.findByBatchName(batchName)).thenReturn(Optional.of(batch));

		List<Long> result = batchService.getEmployeesInBatchByName(batchName);

		assertEquals(employeeIds, result);

		verify(batchRepository, times(1)).findByBatchName(batchName);
	}

	
	@Test
	public void testGetEmployeesInBatchByNameBatchEmpty() {
		String batchName = "EmptyBatch";

		Batch batch = new Batch();
		batch.setBatchName(batchName);
		batch.setEmployeeId(new ArrayList<>());

		when(batchRepository.findByBatchName(batchName)).thenReturn(Optional.of(batch));

		Exception exception = assertThrows(BatchEmptyException.class,
				() -> batchService.getEmployeesInBatchByName(batchName));

		assertEquals("No employees found in batch with name " + batchName, exception.getMessage());

		verify(batchRepository, times(1)).findByBatchName(batchName);
	}

	
	@Test
	public void testGetEmployeesInBatchByNameBatchNotFound() {
		String batchName = "NonExistentBatch";

		when(batchRepository.findByBatchName(batchName)).thenReturn(Optional.empty());

		Exception exception = assertThrows(BatchNotFoundException.class,
				() -> batchService.getEmployeesInBatchByName(batchName));

		assertEquals("Batch with name " + batchName + " not found.", exception.getMessage());

		verify(batchRepository, times(1)).findByBatchName(batchName);
	}

	
	@Test
    void testGetAllBatchNamesWithIdsWhenBatchesNotEmpty() {
        // Mocking data
        List<Batch> batches = new ArrayList<>();
        Batch batch1 = new Batch(1L, "Batch A", "Description A", LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 30), List.of(101L, 102L), 50L);
        Batch batch2 = new Batch(2L, "Batch B", "Description B", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 31), List.of(103L, 104L), 60L);
        batches.add(batch1);
        batches.add(batch2);

        // Mocking repository method
        when(batchRepository.findAll()).thenReturn(batches);

        // Calling the service method
        List<Map<String, Object>> result = batchService.getAllBatchNamesWithIds();

        // Verifying result
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        // Verifying content
        Map<String, Object> batch1Map = result.get(0);
        assertEquals(1L, batch1Map.get("batchId"));
        assertEquals("Batch A", batch1Map.get("batchName"));

        Map<String, Object> batch2Map = result.get(1);
        assertEquals(2L, batch2Map.get("batchId"));
        assertEquals("Batch B", batch2Map.get("batchName"));

        // Verifying repository method invocation
        verify(batchRepository, times(1)).findAll();
    }

	
    @Test
    void testGetAllBatchNamesWithIdsWhenBatchesEmpty() {
        // Mocking repository method to return an empty list
        when(batchRepository.findAll()).thenReturn(new ArrayList<>());

        // Calling the service method
        assertThrows(BatchEmptyException.class, () -> {
            batchService.getAllBatchNamesWithIds();
        });

        // Verifying repository method invocation
        verify(batchRepository, times(1)).findAll();
    }
	
	
	@Test
	public void testDeleteBatchById() {
		Long batchId = 1L;

		Batch batch = new Batch();
		batch.setBatchId(batchId);

		when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
		doNothing().when(batchRepository).delete(batch);

		assertDoesNotThrow(() -> batchService.deleteBatchById(batchId));

		verify(batchRepository, times(1)).findById(batchId);
		verify(batchRepository, times(1)).delete(batch);
	}

	
	@Test
	public void testDeleteBatchByIdBatchNotFound() {
		Long batchId = 1L;

		when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

		assertThrows(BatchNotFoundException.class, () -> batchService.deleteBatchById(batchId));

		verify(batchRepository, times(1)).findById(batchId);
		verify(batchRepository, never()).delete(any());
	}

	
	@Test
	public void testDeleteBatchByName() {
		String batchName = "Test Batch";

		Batch batch = new Batch();
		batch.setBatchName(batchName);

		when(batchRepository.findByBatchName(batchName)).thenReturn(Optional.of(batch));
		doNothing().when(batchRepository).delete(batch);

		assertDoesNotThrow(() -> batchService.deleteBatchByName(batchName));

		verify(batchRepository, times(1)).findByBatchName(batchName);
		verify(batchRepository, times(1)).delete(batch);
	}

	
	@Test
	public void testDeleteBatchByNameBatchNotFound() {
		String batchName = "Test Batch";

		when(batchRepository.findByBatchName(batchName)).thenReturn(Optional.empty());

		assertThrows(BatchNotFoundException.class, () -> batchService.deleteBatchByName(batchName));

		verify(batchRepository, times(1)).findByBatchName(batchName);
		verify(batchRepository, never()).delete(any());
	}


	@Test
	public void testDeleteEmployeeFromBatchEmployeeNotFound() {
	    
	    Long batchId = 1L;
	    Long employeeId = 1004L;
	    Batch batch = new Batch();
	    batch.setBatchId(batchId);
	    batch.setEmployeeId(Arrays.asList(1001L, 1002L, 1003L));
	    Mockito.when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

	    
	    assertThrows(EmployeeNotFoundException.class, () -> batchService.deleteEmployeeFromBatch(batchId, employeeId));
	}

	
	@Test
	public void testDeleteEmployeeFromBatchBatchNotFound() {
	    
	    Long batchId = 1L; 
	    Long employeeId = 1001L;
	    Mockito.when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

	    
	    assertThrows(BatchNotFoundException.class, () -> batchService.deleteEmployeeFromBatch(batchId, employeeId));
	}


	@Test
    void testUpdateEndDateWhenBatchExists() {
        // Mock data
        Long batchId = 1L;
        LocalDate endDate = LocalDate.of(2024, 4, 30);
        Batch batch = new Batch(batchId, "Batch A", "Description", LocalDate.of(2024, 4, 1),
                endDate, null, 50L);

        // Mock repository method
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        // Calling the service method
        assertDoesNotThrow(() -> {
            batchService.updateEndDate(batchId, endDate);
        });

        // Verifying repository method invocation
        verify(batchRepository, times(1)).findById(batchId);
        verify(batchRepository, times(1)).save(batch);
    }

	
    @Test
    void testUpdateEndDateWhenBatchNotExists() {
        // Mock data
        Long batchId = 1L;
        LocalDate endDate = LocalDate.of(2024, 4, 30);

        // Mock repository method
        when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

        // Calling the service method and verifying exception
        BatchNotFoundException exception = assertThrows(BatchNotFoundException.class, () -> {
            batchService.updateEndDate(batchId, endDate);
        });

        assertEquals("Batch with id " + batchId + " not found", exception.getMessage());

        // Verifying repository method invocation
        verify(batchRepository, times(1)).findById(batchId);
        verify(batchRepository, never()).save(any());
    }

	
	@Test
	public void testUpdateBatchName() {
		Long batchId = 1L;
		String batchName = "New Batch Name";

		Batch batch = new Batch();
		batch.setBatchId(batchId);
		batch.setBatchName("Old Batch Name");

		when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
		when(batchRepository.existsByBatchName(batchName)).thenReturn(false);

		batchService.updateBatchName(batchId, batchName);

		assertEquals(batchName, batch.getBatchName());
		verify(batchRepository, times(1)).save(batch);
	}
	
	@Test
    public void testUpdateBatch() {
        // Mock data
        Long batchId = 1L;
        Batch batch = new Batch();
        batch.setBatchName("TestBatch");
        batch.setBatchDescription("Description");
        batch.setStartDate(LocalDate.now());
        batch.setEndDate(LocalDate.now());
        batch.setBatchSize(10L);

        // Mock behavior of findById
        Batch existingBatch = new Batch();
        existingBatch.setBatchId(batchId);
        existingBatch.setBatchName("ExistingBatch");
        existingBatch.setBatchDescription("Existing Description");
        existingBatch.setStartDate(LocalDate.now());
        existingBatch.setEndDate(LocalDate.now());
        existingBatch.setBatchSize(5L);
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(existingBatch));

        // Call the method
        batchService.updateBatch(batchId, batch);

        // Verify that batchRepository.save(existingBatch) was called once
        verify(batchRepository, times(1)).save(existingBatch);

        // Verify that the existingBatch has been updated with the new values
        assertEquals("TestBatch", existingBatch.getBatchName());
        assertEquals("Description", existingBatch.getBatchDescription());
        
    }

    @Test
    public void testUpdateBatchNotFound() {
        // Mock data
        Long batchId = 1L;
        Batch batch = new Batch();
        batch.setBatchName("TestBatch");
        batch.setBatchDescription("Description");
        batch.setStartDate(LocalDate.now());
        batch.setEndDate(LocalDate.now());
        batch.setBatchSize(10L);

        // Mock behavior of findById
        when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

        // Call the method, should throw BatchNotFoundException
        assertThrows(BatchNotFoundException.class, () -> batchService.updateBatch(batchId, batch));
    }

    
    @Test
    public void testDeleteEmployeesFromBatchBatchNotFound() {
        
        Long batchId = 1L;
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);

        // Mock the batchRepository behavior
        when(batchRepository.findById(batchId)).thenReturn(Optional.empty());

        // Call the service method and assert the exception
        assertThrows(BatchNotFoundException.class, () -> {
            batchService.deleteEmployeesFromBatch(batchId, employeeIds);
        });
    }

    
    @Test
    public void testDeleteEmployeesFromBatchEmployeeNotFound() {
        
        Long batchId = 1L;
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);

        // Mock the batchRepository behavior
        Batch batch = new Batch();
        batch.setBatchId(batchId);
        batch.setEmployeeId(new ArrayList<>());
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        // Call the service method and assert the exception
        assertThrows(EmployeeNotFoundException.class, () -> {
            batchService.deleteEmployeesFromBatch(batchId, employeeIds);
        });
    }

    
}
