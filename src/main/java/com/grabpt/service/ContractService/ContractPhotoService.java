package com.grabpt.service.ContractService;

import com.grabpt.domain.entity.Contract;
import org.springframework.web.multipart.MultipartFile;

public interface ContractPhotoService {
	public Contract uploadUserSign(Long contractId, MultipartFile file);
	public Contract uploadProSign(Long contractId, MultipartFile file);
}
