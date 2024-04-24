package com.thbs.BatchManagement.entity;

public class EmployeeDTO {


	    private Long employeeId;

		public Long getEmployeeId() {
			return  employeeId;
		}

		public void setEmployeeId(Long  employeeId) {
			this. employeeId =  employeeId;
		}

		public EmployeeDTO(Long employeeId) {
			super();
			this.employeeId = employeeId;
		}

		public EmployeeDTO() {
			super();
		}
		

		
}

