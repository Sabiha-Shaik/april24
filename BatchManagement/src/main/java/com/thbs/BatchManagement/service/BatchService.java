package com.thbs.BatchManagement.service;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.thbs.BatchManagement.entity.Batch;
import com.thbs.BatchManagement.entity.EmployeeDTO;
import com.thbs.BatchManagement.exceptionhandler.BatchEmptyException;
import com.thbs.BatchManagement.exceptionhandler.BatchNotFoundException;
import com.thbs.BatchManagement.exceptionhandler.DuplicateBatchFoundException;
import com.thbs.BatchManagement.exceptionhandler.DuplicateEmployeeException;
import com.thbs.BatchManagement.exceptionhandler.EmployeeNotFoundException;
import com.thbs.BatchManagement.exceptionhandler.EmptyEmployeesListException;
import com.thbs.BatchManagement.exceptionhandler.EmptyFileException;
import com.thbs.BatchManagement.repository.BatchRepository;


@Service
public class BatchService {

	@Autowired
	private BatchRepository batchRepository; 
	
	@Autowired
    private RestTemplate restTemplate;
    
	
	// adding trainees with batch creation
	public ResponseEntity<String> createBatch(Batch batch) {
		// Check if batch with the same name already exists
		if (batchRepository.existsByBatchName(batch.getBatchName())) { 
			throw new DuplicateBatchFoundException("Batch already exists");
		}
		// Save the batch
		batchRepository.save(batch);
		return ResponseEntity.status(HttpStatus.CREATED).body("Batch created successfully");
	}

	
	// parse excel
	public List<EmployeeDTO> parseExcel(MultipartFile file) throws IOException {
	    Set<Long> employeeIds = new HashSet<>();
	    List<EmployeeDTO> employees = new ArrayList<>();
	    
	    if (file.isEmpty()) {
	        throw new EmptyFileException("The supplied file was empty (zero bytes long)");
	    }

	    Workbook workbook;
	    try {
	        workbook = WorkbookFactory.create(file.getInputStream());
	    } catch (InvalidFormatException e) {
	        throw new IOException("Invalid file format", e);
	    }

	    Sheet sheet = workbook.getSheetAt(0);
	    Iterator<Row> rowIterator = sheet.iterator();

	    while (rowIterator.hasNext()) {
	        Row row = rowIterator.next();
	        EmployeeDTO employee = new EmployeeDTO();
	        Cell cell = row.getCell(0); // Assuming employee ID is in the first column

	        if (cell != null && cell.getCellType() == CellType.NUMERIC) {
	            Long employeeId = (long) cell.getNumericCellValue();
	            if (!employeeIds.contains(employeeId)) {
	                employee.setEmployeeId(employeeId);
	                // Set other employee details as needed from other columns
	                // Example: employee.setName(row.getCell(1).getStringCellValue());
	                employees.add(employee);
	                employeeIds.add(employeeId);
	            }
	        }
	    }
	    workbook.close();

	    if (employees.isEmpty()) {
	        throw new EmptyEmployeesListException("No employees found in the Excel file");
	    }

	    return employees;
	}

	
	// bulk upload with batch creation
	public void addEmployeesToBatchFromExcel(List<EmployeeDTO> employees, String data) throws IOException, ParseException {
	    // Create a Batch entity and set the trainee IDs
	    List<Long> employeeIds = employees.stream().map(EmployeeDTO::getEmployeeId).collect(Collectors.toList());

	    Batch batch = new Batch();

	    // Parse data as JSON object
	    ObjectMapper objectMapper = new ObjectMapper();
	    JsonNode jsonNode = objectMapper.readTree(data);

	    // Set fields of Batch entity from JSON object
	    if (jsonNode.has("batchName"))
	        batch.setBatchName(jsonNode.get("batchName").asText());
	    if (jsonNode.has("batchDescription"))
	        batch.setBatchDescription(jsonNode.get("batchDescription").asText());
	    if (jsonNode.has("startDate")) {
	        // Parse startDate string to Date
	        String startDateStr = jsonNode.get("startDate").asText();
	        LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	        batch.setStartDate(startDate);	     
	    }
	    if (jsonNode.has("endDate")) {
	        // Parse endDate string to Date
	        String endDateStr = jsonNode.get("endDate").asText();
	       
	        // Assuming endDateStr is a string in the format "yyyy-MM-dd"
	        LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	        batch.setEndDate(endDate);
	    }

	    if (jsonNode.has("batchSize"))
	        batch.setBatchSize(jsonNode.get("batchSize").asLong());
	    if (batchRepository.existsByBatchName(batch.getBatchName())) {
	        throw new DuplicateBatchFoundException("Batch already exists");
	    }

	    batch.setEmployeeId(employeeIds);
	    // Save the batch to the database
	    batchRepository.save(batch);
	}

	
	// adding employees to existing batch by batchid
	private Map<Long, List<Long>> batchEmployeeMap = new HashMap<>();
	public void addEmployeesToExistingBatches(Long batchId, List<EmployeeDTO> employees) {
	        Batch batch = batchRepository.findById(batchId)
	                .orElseThrow(() -> new BatchNotFoundException("Batch not found"));

	        if (employees.isEmpty()) {
	            throw new EmptyEmployeesListException("No employees to add");
	        }

	        List<Long> newEmployeeIds = new ArrayList<>();
	        // Add new employees to the existing batch
	        for (EmployeeDTO employeeDTO : employees) {
	            Long employeeId = employeeDTO.getEmployeeId();
	            if (batch.getEmployeeId().contains(employeeId)) {
	                // If employee already exists in batch, handle accordingly
	                // For example, you can throw a DuplicateEmployeeException
	                throw new DuplicateEmployeeException("Employee with ID " + employeeId + " already exists in batch");
	            }
	            batch.getEmployeeId().add(employeeId);
	            newEmployeeIds.add(employeeId);
	        }

	        batchEmployeeMap.put(batchId, newEmployeeIds);
	        try {
	            batchRepository.save(batch);

	            // Call getBatchEmployeeMap() method by passing batchEmployeeMap as argument
	            String jsonData = getBatchEmployeeMap(batchEmployeeMap);
	            // Call postBatchEmployeeMap() method with the result
	            postBatchEmployeeMap(jsonData);
	        } catch (HttpClientErrorException ex) {
	            // Handle HttpClientErrorException appropriately
	            System.err.println("Error making HTTP request: " + ex.getMessage());
	            // You may choose to rethrow the exception or handle it as needed
	        }
	 }

		
	// bulk upload to existing batch by batchid
	public void addEmployeesToExistingBatchesFromExcel(Long batchId, List<EmployeeDTO> employees) throws BatchNotFoundException, DuplicateEmployeeException {
	    Optional<Batch> optionalBatch = batchRepository.findById(batchId);
	    if (optionalBatch.isPresent()) {
	        Batch batch = optionalBatch.get();

	        List<Long> newEmployeeIds = new ArrayList<>();
	        List<Long> existingEmployeeIds = new ArrayList<>();

	        for (EmployeeDTO employee : employees) {
	            Long employeeId = employee.getEmployeeId();
	            if (batch.getEmployeeId().contains(employeeId)) {
	                existingEmployeeIds.add(employeeId); // If employee already exists in batch
	            } else {
	                newEmployeeIds.add(employeeId); // If employee is new to batch
	            }
	        }

	        if (newEmployeeIds.isEmpty() && !existingEmployeeIds.isEmpty()) {
	            throw new DuplicateEmployeeException("All employees provided are already present in this batch");
	        }

	        if (!newEmployeeIds.isEmpty()) {
	            batch.getEmployeeId().addAll(newEmployeeIds);
	            batchRepository.save(batch);

	            // Adding batch ID and new employee IDs to the batchEmployeeMap
	            batchEmployeeMap.put(batchId, newEmployeeIds);

	            try {
	                String jsonData = getBatchEmployeeMap(batchEmployeeMap);
	                postBatchEmployeeMap(jsonData);
	                // Handle jsonData as needed
	            } catch (HttpClientErrorException ex) {
	                // Handle HttpClientErrorException appropriately
	                System.err.println("Error making HTTP request in getBatchEmployeeMap: " + ex.getMessage());
	                // You may choose to rethrow the exception or handle it as needed
	            }
	        }
	    } else {
	        throw new BatchNotFoundException("Batch not found");
	    }
	}

	
	// list of batchnames
	public List<String> getAllBatchNames() {
		List<Batch> batches = batchRepository.findAll();
		if (batches.isEmpty()) {
			throw new BatchEmptyException("Batches are not created yet");
		}
		return batches.stream().map(Batch::getBatchName).collect(Collectors.toList());
	}

	
	// list of batch details by batchid
	public ResponseEntity<Object> getBatchById(@PathVariable Long batchId) {
		Batch batch = batchRepository.findById(batchId).orElse(null);
		if (batch != null) {
			return ResponseEntity.ok(batch);
		} else {

			throw new BatchNotFoundException("Batch not found with id " + batchId);
		}
	}

	
	// list of batch details by batchname
	public ResponseEntity<Object> getBatchByName(@PathVariable String batchName) {

		Batch batch = batchRepository.findByBatchName(batchName).orElse(null);
		if (batch != null) {
			return ResponseEntity.ok(batch);
		} else {

			throw new BatchNotFoundException("Batch not found with name " + batchName);
		}
	}

	 
	// list of all batch details
	public List<Batch> getAllBatches() {
		List<Batch> batches = batchRepository.findAll();
		if (batches.isEmpty()) {
			throw new BatchEmptyException("Batches are not created yet");
		}
		return batches;
	}

	
	// list of employees using batchid
	public List<Long> getEmployeesInBatch(Long batchId) {
		Batch batch = batchRepository.findById(batchId).orElse(null);
		if (batch != null) {
			List<Long> employeeIds = batch.getEmployeeId();
			if (!employeeIds.isEmpty()) {
				return employeeIds;
			} else {
				throw new BatchEmptyException("No employees found in batch with id " + batchId);
			}
		} else {
			throw new BatchNotFoundException("Batch with id " + batchId + " not found.");
		}
	}

	
	// list of employees using batchname
	public List<Long> getEmployeesInBatchByName(String batchName) {
		Batch batch = batchRepository.findByBatchName(batchName).orElse(null);
		if (batch != null) {
			List<Long> employeeIds = batch.getEmployeeId();
			if (!employeeIds.isEmpty()) {
				return employeeIds;
			} else {
				throw new BatchEmptyException("No employees found in batch with name " + batchName);
			}
		} else {
			throw new BatchNotFoundException("Batch with name " + batchName + " not found.");
		}
	}

	
	// list of all batch names along with ids
	public List<Map<String, Object>> getAllBatchNamesWithIds() {
	        List<Batch> batches = batchRepository.findAll();
	        if (batches.isEmpty()) {
	            throw new BatchEmptyException("Batches are not created yet");
	        } else {
	            List<Map<String, Object>> batchNamesWithIds = new ArrayList<>();
	            for (Batch batch : batches) {
	                Map<String, Object> batchMap = new HashMap<>();
	                batchMap.put("batchId", batch.getBatchId());
	                batchMap.put("batchName", batch.getBatchName());
	                batchNamesWithIds.add(batchMap);
	            }
	            return batchNamesWithIds;
	        }
	 } 
	
	
	// list of all employee-details in batch by batchid	
	public List<Map<String, Object>> getEmployeesInBatchWithDetails(Long batchId) {
	    Batch batch = batchRepository.findById(batchId).orElse(null);
	    if (batch != null) {
	        List<Long> employeeIds = batch.getEmployeeId();
	        List<Map<String, Object>> mergedEmployeeDetails = fetchMergedEmployeeIds();
	        
	        // Filter employee details based on the employee IDs in the batch
	        List<Map<String, Object>> employeesInBatch = mergedEmployeeDetails.stream()
	                .filter(employee -> {
	                    Object idObj = employee.get("employeeId");
	                    if (idObj instanceof Number) {
	                        Long id = ((Number) idObj).longValue();
	                        return employeeIds.contains(id); 
	                    }
	                    return false;
	                })
	                .collect(Collectors.toList());
	        return employeesInBatch;
	    } else {
	        throw new BatchNotFoundException("Batch with id " + batchId + " not found.");
	    }
	}
	
	
	// list of all batch names based on emloyeeid they are associated
	public List<Map<String, Object>> getBatchesByEmployeeId(Long employeeId) {
	    List<Batch> batches = batchRepository.findAll();
	    List<Map<String, Object>> result = new ArrayList<>();

	    for (Batch batch : batches) {
	        List<Long> employeeIds = batch.getEmployeeId();
	        if (employeeIds != null && employeeIds.contains(employeeId)) {
	            Map<String, Object> batchInfo = new HashMap<>();
	            batchInfo.put("batchId", batch.getBatchId());
	            batchInfo.put("batchName", batch.getBatchName());
	            result.add(batchInfo);
	        }
	    }

	    return result;
	}


	// list of all new users   
	public String getBatchEmployeeMap(Map<Long, List<Long>> batchEmployeeMap) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n");
        for (Map.Entry<Long, List<Long>> entry : batchEmployeeMap.entrySet()) {
            stringBuilder.append("  \"batchId\": ").append(entry.getKey()).append(",\n");
            stringBuilder.append("  \"userIds\": ").append(entry.getValue()).append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

		
	
	// deleting batch with batchid
	public void deleteBatchById(Long batchId) {
		Batch batch = batchRepository.findById(batchId).orElse(null);
		if (batch != null) {
			batchRepository.delete(batch);
		} else {
			throw new BatchNotFoundException("Batch not found");
		}
	}

	
	// deleting batch with batchname
	public void deleteBatchByName(String batchName) {
		Batch batch = batchRepository.findByBatchName(batchName).orElse(null);
		if (batch != null) {
			batchRepository.delete(batch);
		} else {
			throw new BatchNotFoundException("Batch not found");
		}
	}


	// deleting employee with batchid
	private Map<Long, List<Long>> deleteEmployeeMap = new HashMap<>();
	public void deleteEmployeeFromBatch(Long batchId, long employeeId) {
	    try {
	        Batch batch = batchRepository.findById(batchId)
	                .orElseThrow(() -> new BatchNotFoundException("Batch not found"));

	        if (batch.getEmployeeId().contains(employeeId)) {

	            // Store the deleted employee information before deletion
	            List<Long> employeeList = deleteEmployeeMap.getOrDefault(batchId, new ArrayList<>());
	            employeeList.add(employeeId);
	            deleteEmployeeMap.put(batchId, employeeList);

	            batch.setEmployeeId(batch.getEmployeeId().stream()
	                    .filter(id -> id != employeeId)
	                    .collect(Collectors.toList()));
	            System.out.println(deleteEmployeeMap);

	            batchRepository.save(batch);
	            String jsonData1 = getDeleteEmployeeMap(deleteEmployeeMap);

	        } else {
	            throw new EmployeeNotFoundException("Employee not found in the batch");
	        }
	    } catch (HttpClientErrorException ex) {
	        // If an error occurs while making the HTTP request, handle the exception
	        System.err.println("Error making HTTP request: " + ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        // Handle the exception as needed, e.g., logging, throwing a custom exception, etc.
	    }
	}

	
	//deleting  multiple employees with batchids
	public void deleteEmployeesFromBatch(Long batchId, List<Long> employeeIds) {
	    try {
	        Batch batch = batchRepository.findById(batchId)
	                .orElseThrow(() -> new BatchNotFoundException("Batch not found"));

	        List<Long> existingEmployeeIds = batch.getEmployeeId();
	        Map<Long, List<Long>> deleteEmployeeMap = new HashMap<>(); // Map to store deleted employees

	        // Iterate through employeeIds to remove them from the batch
	        for (Long employeeId : employeeIds) {
	            if (!existingEmployeeIds.contains(employeeId)) {
	                throw new EmployeeNotFoundException("Employee not found in the batch: " + employeeId);
	            }
	            existingEmployeeIds.remove(employeeId);

	            // Add the deleted employee to the deleteEmployeeMap
	            if (!deleteEmployeeMap.containsKey(batchId)) {
	                deleteEmployeeMap.put(batchId, new ArrayList<>());
	            }
	            deleteEmployeeMap.get(batchId).add(employeeId);
	        }

	        batch.setEmployeeId(existingEmployeeIds);
	        System.out.println(deleteEmployeeMap);
	        batchRepository.save(batch);

	        // Now deleteEmployeeMap contains all the deleted employee IDs with their corresponding batch IDs
	        // You can use this map as needed
	        String jsonData1 = getDeleteEmployeeMap(deleteEmployeeMap);
	    } catch (HttpClientErrorException ex) {
	        // If an error occurs while making the HTTP request, handle the exception
	        System.err.println("Error making HTTP request: " + ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
	        // Handle the exception as needed, e.g., logging, throwing a custom exception, etc.
	    }
	}


	// list of all deleting users    
	private String getDeleteEmployeeMap(Map<Long, List<Long>> deleteEmployeeMap) {
	        StringBuilder stringBuilder = new StringBuilder();
	        stringBuilder.append("{\n");
	        for (Map.Entry<Long, List<Long>> entry : deleteEmployeeMap.entrySet()) {
	            stringBuilder.append("  \"batchId\": ").append(entry.getKey()).append(",\n");
	            stringBuilder.append("  \"userIds\": ").append(entry.getValue()).append("\n");
	        }
	        stringBuilder.append("}");
	        String result = stringBuilder.toString();

	        System.out.println(result);
	        // Call postBatchEmployeeMap() method with the result
	        postDeleteEmployeeMap(result);

	        return result;
	}
	 

	// Updating end date with id
	public void updateEndDate(Long batchId, LocalDate endDate) {
	    Optional<Batch> optionalBatch = batchRepository.findById(batchId);
	    if (optionalBatch.isPresent()) {
	        Batch batch = optionalBatch.get();
	        batch.setEndDate(endDate);
	        batchRepository.save(batch);
	    } else {
	        throw new BatchNotFoundException("Batch with id " + batchId + " not found");
	    }
	}
	
  
	// renaming batchname with id
	public void updateBatchName(Long batchId, String batchName) {
		Optional<Batch> optionalBatch = batchRepository.findById(batchId);
		if (optionalBatch.isPresent()) {
			Batch batch = optionalBatch.get();
			if (!batch.getBatchName().equals(batchName) && batchRepository.existsByBatchName(batchName)) {
				throw new DuplicateBatchFoundException("Batch with name '" + batchName + "' already exists");
			}
			batch.setBatchName(batchName);
			batchRepository.save(batch);
		} else {
			throw new BatchNotFoundException("Batch not found with batchId: " + batchId);
		}
	}  
	
	
	// edit batch details
	public void updateBatch(Long batchId, Batch batch) {
        Batch existingBatch = batchRepository.findById(batchId).orElse(null);

        if (existingBatch != null) {
        	
            existingBatch.setBatchName(batch.getBatchName());
            existingBatch.setBatchDescription(batch.getBatchDescription());
            existingBatch.setStartDate(batch.getStartDate());
            existingBatch.setEndDate(batch.getEndDate());
            existingBatch.setBatchSize(batch.getBatchSize());
 
            batchRepository.save(existingBatch); 
        } 
        else {
        	throw new BatchNotFoundException("Batch with id " + batchId + " not found");
        }
    }
	
	
	// remaining employees
	public List<Map<String, Object>> findRemainingEmployees(Long batchId, List<Integer> allEmployeeIds) {
	    List<Map<String, Object>> mergedEmployeeDetails = fetchMergedEmployeeIds();
	    List<Long> employeesInBatch;
	    try {
	        employeesInBatch = getEmployeesInBatch(batchId);
	    } catch (BatchEmptyException ex) {
	        // If batch is empty, return all employee IDs
	        return mergedEmployeeDetails.stream()
	                .filter(employee -> allEmployeeIds.contains((Integer) employee.get("employeeId")))
	                .collect(Collectors.toList());
	    }

	    Set<Integer> employeesInBatchIds = employeesInBatch.stream()
	            .map(Long::intValue)
	            .collect(Collectors.toSet());

	    List<Integer> remainingEmployeeIds = allEmployeeIds.stream()
	            .filter(id -> !employeesInBatchIds.contains(id))
	            .collect(Collectors.toList());

	    List<Map<String, Object>> remainingEmployees = mergedEmployeeDetails.stream()
	            .filter(employee -> remainingEmployeeIds.contains((Integer) employee.get("employeeId")))
	            .collect(Collectors.toList());

	    return remainingEmployees;
	}

	
	// fetching all users
	public List<Map<String, Object>> fetchMergedEmployeeIds() {
	    List<Map<String, Object>> mergedEmployeeIds = new ArrayList<>();

	    // Fetch data from the first path
	    String firstPathUrl = "http://user-service/user/role/trainee";
	    ResponseEntity<List<Map<String, Object>>> firstPathResponse = restTemplate.exchange(
	            firstPathUrl,
	            HttpMethod.GET,
	            new HttpEntity<>(null),
	            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
	    );
	    

	    if (firstPathResponse.getStatusCode() == HttpStatus.OK) {
	        List<Map<String, Object>> firstPathEmployeeData = firstPathResponse.getBody();
	        if (firstPathEmployeeData != null) {
	            mergedEmployeeIds.addAll(firstPathEmployeeData);
	        }
	    }

	 return mergedEmployeeIds;
	}
	
	
	// post data to endpoint
	public String postBatchEmployeeMap(String requestData) {
        // Define the URL of the endpoint
        String url = "http://learning-resource-service/user-progress/update-progress";
        System.out.println(requestData);
        // Set the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the HttpEntity with the JSON payload and headers
        HttpEntity<String> request = new HttpEntity<>(requestData, headers);

        // Make the POST request using RestTemplate
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // Get the response body
            String responseBody = response.getBody();

            // Handle the response if needed
            // For example, check the response status code
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("POST request was successful");
            } else {
                System.out.println("POST request failed");
            }

            // Clear the batchEmployeeMap after posting data
            batchEmployeeMap.clear();

            // Return the response body
            return responseBody;
        } catch (HttpClientErrorException ex) {
            // If an error occurs, handle the exception
            // Print the error response
            System.err.println("Error response from server: " + ex.getResponseBodyAsString());
            throw ex; // Rethrow the exception or handle it as needed
        }
    }

	// deleting users in post rest template call 
	private String postDeleteEmployeeMap(String jsonPayload) {
	    // Define the URL of the endpoint
	    String url = "http://learning-resource-service/user-progress/delete-progress";

	    // Set the request headers
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    // Create the HttpEntity with the JSON payload and headers
	    HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

	    try {
	        // Make the DELETE request using RestTemplate
	        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

	        // Handle the response if needed
	        // For example, check the response status code
	        if (response.getStatusCode().is2xxSuccessful()) {
	            System.out.println("DELETE request was successful");
	        } else {
	            System.out.println("DELETE request failed");
	        }

	        // Return the response body
	        deleteEmployeeMap.clear();
	        return response.getBody();
	    } catch (HttpClientErrorException ex) {
	        // If an error occurs, handle the exception
	        // Print the error response
	        System.err.println("Error response from server: " + ex.getResponseBodyAsString());
	        throw ex; // Rethrow the exception or handle it as needed
	    }
	}
	    
	    

}
