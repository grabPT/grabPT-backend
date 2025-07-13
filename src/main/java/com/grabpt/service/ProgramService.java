package com.grabpt.service;

import com.grabpt.domain.entity.ProProfile;
import com.grabpt.dto.request.ProgramRequestDTO;
import java.util.List;

public interface ProgramService {
	void updatePrograms(ProProfile proProfile, List<ProgramRequestDTO> programDTOs);
}
