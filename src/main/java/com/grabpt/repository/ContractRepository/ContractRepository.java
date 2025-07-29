package com.grabpt.repository.ContractRepository;

import com.grabpt.domain.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {
}
