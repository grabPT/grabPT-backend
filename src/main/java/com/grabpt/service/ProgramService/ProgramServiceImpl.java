package com.grabpt.service.ProgramService;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.ProProgram;
import com.grabpt.dto.request.ProgramRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {
	@Override
	@Transactional
	public void updatePrograms(ProProfile proProfile, List<ProgramRequestDTO> programDTOs) {
		proProfile.getPrograms().clear();
		if (programDTOs != null) {
			programDTOs.forEach(dto -> {
				ProProgram program = ProProgram.builder()
					.title(dto.getTitle())
					.description(dto.getDescription())
					.pricePerSession(dto.getPricePerSession())
					.totalSessions(dto.getTotalSessions())
					.proProfile(proProfile)
					.build();
				proProfile.getPrograms().add(program);
			});
		}
	}
}
