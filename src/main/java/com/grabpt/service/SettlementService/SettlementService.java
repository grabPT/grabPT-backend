package com.grabpt.service.SettlementService;

import com.grabpt.dto.response.TrainerDashboardDto;

public interface SettlementService {

	TrainerDashboardDto getTrainerDashboard(Long proProfileId, int page, int size);

}
