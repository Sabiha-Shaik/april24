package com.thbs.BatchManagement.entitytest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.thbs.BatchManagement.entity.EmployeeDTO;

public class EmployeeDTOTest {

    private EmployeeDTO employeeDTO;

    @BeforeEach
    public void setUp() {
        employeeDTO = new EmployeeDTO();
    }

    
    @Test
    public void testEmployeeId() {
        assertNull(employeeDTO.getEmployeeId());
        Long employeeId = 1L;
        employeeDTO.setEmployeeId(employeeId);
        assertEquals(employeeId, employeeDTO.getEmployeeId());
    }
    
    
}

