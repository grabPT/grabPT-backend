package com.grabpt.service.Impl;

import com.grabpt.domain.entity.ProProgram;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.ProgramRequestDTO;
import com.grabpt.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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
