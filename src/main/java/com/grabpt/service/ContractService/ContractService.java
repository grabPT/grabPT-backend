package com.grabpt.service.ContractService;

import com.grabpt.domain.entity.*;
import com.grabpt.dto.request.ContractRequest;

public interface ContractService{
	public Contract createContract(Matching matching, Requestions req, Suggestions sug);
	public Contract writeUserInfo(Long contractId, ContractRequest.ContractInfoDto request);
	public Contract writeProInfo(Long contractId, ContractRequest.ContractInfoForProDto request);
	public Contract findById(Long contractId);
	public String generateAndSavePdfToS3(Long contractId);
}

