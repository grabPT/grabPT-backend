package com.grabpt.service.ContractService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ContractHandler;
import com.grabpt.aws.s3.AmazonS3Manager;
import com.grabpt.aws.s3.Uuid;
import com.grabpt.domain.entity.Contract;
import com.grabpt.repository.ContractRepository.ContractRepository;
import com.grabpt.repository.UuidRepository.UuidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ContractPhotoServiceImpl implements ContractPhotoService {
	private final AmazonS3Manager amazonS3Manager;
	private final UuidRepository uuidRepository;
	private final ContractRepository contractRepository;

	@Override
	@Transactional
	public Contract uploadUserSign(Long contractId, MultipartFile file) {
		return uploadSign(contractId, file, true);
	}

	@Override
	@Transactional
	public Contract uploadProSign(Long contractId, MultipartFile file) {
		return uploadSign(contractId, file, false);
	}

	private Contract uploadSign(Long contractId, MultipartFile file, boolean isUser) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		Uuid uuid = Uuid.builder()
			.uuid(java.util.UUID.randomUUID().toString())
			.build();
		uuidRepository.save(uuid);

		String keyName = amazonS3Manager.generateContractPhotoKeyName(uuid);
		String fileUrl = amazonS3Manager.uploadFile(keyName, file);

		Contract contract = contractRepository.findById(contractId)
			.orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));

		if (isUser) {
			contract.getUserInfo().setSignUrl(fileUrl);
		} else {
			contract.getProInfo().setSignUrl(fileUrl);
		}

		return contract;
	}
}
