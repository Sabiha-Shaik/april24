package com.thbs.BatchManagement.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thbs.BatchManagement.entity.Batch;

@Repository
public interface BatchRepository  extends JpaRepository<Batch, Long>{

	
	boolean existsByEmployeeIdContaining(Long employeeId);

    boolean existsByBatchName(String batchName);
    
    Optional<Batch> findByBatchName(String batchName);
    
    Optional<Batch> findById(Long batchId);
    
    
}
