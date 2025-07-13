package com.grabpt.service.ProgramService;

import java.util.List;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.ProgramRequestDTO;

public interface ProgramService {
	void updatePrograms(ProProfile proProfile, List<ProgramRequestDTO> programDTOs);
}
