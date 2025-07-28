package com.grabpt.service.ContractService;

import com.grabpt.apiPayload.code.status.ErrorStatus;
import com.grabpt.apiPayload.exception.handler.ContractHandler;
import com.grabpt.domain.entity.*;
import com.grabpt.domain.enums.MatchingStatus;
import com.grabpt.dto.request.ContractRequest;
import com.grabpt.repository.ContractRepository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
	private final ContractRepository contractRepository;

	@Override
	@Transactional
	public Contract writeUserInfo(Long contractId, ContractRequest.ContractInfoDto request){
		Contract contract = contractRepository.findById(contractId).orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
		contract.getMatching().setStatus(MatchingStatus.USERWROTE);

		ContractInfo userInfo = new ContractInfo();
		userInfo.setAddress(request.getAddress());
		userInfo.setName(request.getName());
		userInfo.setBirth(request.getBirth());
		userInfo.setGender(request.getGender());
		userInfo.setPhoneNumber(request.getPhoneNumber());
		contract.setUserInfo(userInfo);
		return contract;
	}

	@Override
	@Transactional
	public Contract writeProInfo(Long contractId, ContractRequest.ContractInfoDto request){
		Contract contract = contractRepository.findById(contractId).orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
		contract.getMatching().setStatus(MatchingStatus.COMPLETED);

		ContractInfo userInfo = new ContractInfo();
		userInfo.setAddress(request.getAddress());
		userInfo.setName(request.getName());
		userInfo.setBirth(request.getBirth());
		userInfo.setGender(request.getGender());
		userInfo.setPhoneNumber(request.getPhoneNumber());
		contract.setUserInfo(userInfo);
		return contract;
	}

	@Override
	@Transactional
	public Contract createContract(Matching matching, Requestions req, Suggestions sug){
		Contract contract = Contract.builder()
			.matching(matching)
			.price(sug.getPrice())
			.ptAddress(sug.getLocation())
			.startDate(req.getStartPreference())
			.totalSession(req.getSessionCount())
			.build();

		return contractRepository.save(contract);
	}

	@Override
	public Contract findById(Long contractId){
		return contractRepository.findById(contractId)
			.orElseThrow(() -> new ContractHandler(ErrorStatus.CONTRACT_NOT_FOUND));
	}

}
