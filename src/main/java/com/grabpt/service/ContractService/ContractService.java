package com.grabpt.service.ContractService;

import com.grabpt.domain.entity.*;
import com.grabpt.domain.enums.PaymentStatus;
import com.grabpt.domain.enums.Role;
import com.grabpt.dto.request.ContractRequest;
import com.grabpt.dto.response.ContractResponse;
import org.springframework.data.domain.Pageable;

public interface ContractService{
	public Contract createContract(Matching matching, Requestions req, Suggestions sug);
	public Contract writeUserInfo(Long contractId, ContractRequest.ContractInfoDto request);
	public Contract writeProInfo(Long contractId, ContractRequest.ContractInfoForProDto request);
	public Contract findById(Long contractId);
	public String generateAndSavePdfToS3(Long contractId);
	public ContractResponse.ContractListResponseDto getContractList(Role role, Long userId, PaymentStatus paymentStatus, Pageable pageable);
	public void deleteContract(Long contractId, String email);
}

